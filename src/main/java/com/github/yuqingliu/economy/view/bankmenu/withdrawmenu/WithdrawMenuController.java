package com.github.yuqingliu.economy.view.bankmenu.withdrawmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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
import com.github.yuqingliu.economy.view.PageData;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class WithdrawMenuController extends AbstractPlayerInventoryController<BankMenu> {
    private final int[] prevMenuButton = new int[]{1,1};
    private final int[] exitMenuButton = new int[]{2,1};
    private final int[] prevPageButton = new int[]{3,1};
    private final int[] nextPageButton = new int[]{7,1};
    private final int[] currenciesStart = new int[]{4,1};
    private final int currenciesLength = 3;
    private final int currenciesWidth = 1;
    private final int currenciesSize = currenciesLength * currenciesWidth;
    private final List<int[]> currencies;
    private final PageData<CurrencyEntity> pageData = new PageData<>();
    private AccountEntity account;

    public WithdrawMenuController(Player player, Inventory inventory, BankMenu bankMenu) {
        super(player, inventory, bankMenu);
        this.currencies = rectangleArea(currenciesStart, currenciesWidth, currenciesLength);
    }
        
    public void openMenu(AccountEntity account) {
        this.account = account;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.WithdrawMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.ORANGE_STAINED_GLASS_PANE));
        buttons();
        reload();
    }

    public void reload() {
        fetchCurrencies();
        displayCurrencies();
    }

    public void nextPage() {
        pageData.nextPage(() -> displayCurrencies());
    }

    public void prevPage() {
        pageData.prevPage(() -> displayCurrencies());
    }

    public void withdraw(int[] slot) {
        inventory.close();
        CurrencyEntity currency = pageData.get(slot);
        PlayerInventory bank = menu.getPluginManager().getInventoryManager().getInventory(BankMenu.class.getSimpleName());
        bank.setDisplayName(Component.text(account.getBank().getBankName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            inventory = bank.load(player);
            Scheduler.runAsync((task) -> {
                try {
                    menu.getBankService().withdrawPlayerAccount(account, player, Double.parseDouble(userInput), currency.getCurrencyName());
                } catch (Exception e) {
                    menu.getLogger().sendPlayerErrorMessage(player, "Invalid amount");
                }
                menu.getWithdrawMenu().getControllers().getPlayerInventoryController(player, this).openMenu(this.account);
            });
        };        

        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("withdraw amount", NamedTextColor.RED));
        scanner.open(player);
    }

    private void fetchCurrencies() {
        this.account = menu.getBankService().getAccount(this.account.getAccountId());
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

    private void displayCurrencies() {
        Map<List<Integer>, CurrencyEntity> options = pageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, CurrencyEntity> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            CurrencyEntity currency = entry.getValue();
            if(currency == null) {
                setItem(coords, getUnavailableIcon());
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
                setItem(coords, item);
            }
        }

    }

    private void buttons() {
        setItem(prevMenuButton, getPrevMenuIcon());       
        setItem(exitMenuButton, getExitMenuIcon());       
        setItem(nextPageButton, getNextPageIcon());
        setItem(prevPageButton, getPrevPageIcon());
    }
}
