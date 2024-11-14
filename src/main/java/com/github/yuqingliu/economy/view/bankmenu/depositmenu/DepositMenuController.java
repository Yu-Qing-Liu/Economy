package com.github.yuqingliu.economy.view.bankmenu.depositmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class DepositMenuController {
    private final BankMenu bankMenu;
    private final int[] prevMenuButton = new int[]{1,1};
    private final int[] exitMenuButton = new int[]{2,1};
    private final int[] prevPage = new int[]{3,1};
    private final int[] nextPage = new int[]{7,1};
    private final int[] currenciesStart = new int[]{4,1};
    private final int currenciesLength = 3;
    private final int currenciesWidth = 1;
    private final int currenciesSize = currenciesLength * currenciesWidth;
    private final List<int[]> currencies;
    private Map<Player, AccountEntity> accounts = new ConcurrentHashMap<>();
    private Map<Integer, Map<List<Integer>, CurrencyEntity>> pageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> pageNumbers = new ConcurrentHashMap<>();

    public DepositMenuController(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
        this.currencies = bankMenu.rectangleArea(currenciesStart, currenciesWidth, currenciesLength);
    }
    
    public void openDepositMenu(Player player, Inventory inv, AccountEntity account) {
        this.accounts.put(player, account);
        this.pageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            bankMenu.getPlayerMenuTypes().put(player, MenuType.DepositMenu);
        }, Duration.ofMillis(50));
        bankMenu.fill(inv, bankMenu.getBackgroundItems().get(Material.ORANGE_STAINED_GLASS_PANE));
        buttons(inv);
        reload(inv, player);
    }

    public void reload(Inventory inv, Player player) {
        fetchCurrencies(inv, player);
        displayCurrencies(inv, player);
    }

    public void nextPage(Player player, Inventory inv) {
        pageNumbers.get(player)[0]++;
        if(pageData.containsKey(pageNumbers.get(player)[0])) {
            displayCurrencies(inv, player); 
        } else {
            pageNumbers.get(player)[0]--;
        }     
    }

    public void prevPage(Player player, Inventory inv) {
        pageNumbers.get(player)[0]--;
        if(pageNumbers.get(player)[0] > 0) {
            displayCurrencies(inv, player);
        } else {
            pageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        accounts.remove(player);
        pageNumbers.remove(player);
    }

    public void deposit(Inventory inv, Player player, CurrencyEntity currency) {
        AccountEntity account = accounts.get(player);
        inv.close();
        PlayerInventory bank = bankMenu.getPluginManager().getInventoryManager().getInventory(BankMenu.class.getSimpleName());
        bank.setDisplayName(Component.text(account.getBank().getBankName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            Inventory inventory = bank.load(player);
            Scheduler.runAsync((task) -> {
                try {
                    bankMenu.getBankService().depositPlayerAccount(account, player, Double.parseDouble(userInput), currency.getCurrencyName());
                } catch (Exception e) {
                    bankMenu.getLogger().sendPlayerErrorMessage(player, "Invalid amount");
                }
                bankMenu.getDepositMenu().getController().openDepositMenu(player, inventory, account);
            });
        };        

        TextMenu scanner = (TextMenu) bankMenu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("deposit amount", NamedTextColor.RED));
        scanner.open(player);
    }

    private void fetchCurrencies(Inventory inv, Player player) {
        AccountEntity account = bankMenu.getBankService().getAccount(accounts.get(player).getAccountId());
        accounts.put(player, account);
        Set<CurrencyEntity> currencies = account.getCurrencies();
        if(currencies.isEmpty()) {
            return;
        }
        Queue<CurrencyEntity> temp = new ArrayDeque<>();
        temp.addAll(currencies);
        int maxPages = (int) Math.ceil((double) currencies.size() / (double) currenciesSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, CurrencyEntity> options = new LinkedHashMap<>();
            for (int[] coords : this.currencies) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayCurrencies(Inventory inv, Player player) {
        Map<List<Integer>, CurrencyEntity> options = pageData.getOrDefault(pageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, CurrencyEntity> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            CurrencyEntity currency = entry.getValue();
            if(currency == null) {
                bankMenu.setItem(inv, coords, bankMenu.getUnavailable());
            } else {
                double amount = currency.getAmount();
                ItemStack item = currency.getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    Component balance = Component.text("Balance ", NamedTextColor.GRAY).append(Component.text(amount + "$", NamedTextColor.GREEN));
                    List<Component> lore = new ArrayList<>();
                    lore.add(balance);
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                bankMenu.setItem(inv, coords, item);
            }
        }

    }

    private void buttons(Inventory inv) {
        bankMenu.setItem(inv, prevMenuButton, bankMenu.getPrevMenu());       
        bankMenu.setItem(inv, exitMenuButton, bankMenu.getExitMenu());       
        bankMenu.setItem(inv, nextPage, bankMenu.getNextPage());
        bankMenu.setItem(inv, prevPage, bankMenu.getPrevPage());
    }
}
