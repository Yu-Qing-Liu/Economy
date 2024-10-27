package com.github.yuqingliu.economy.view.bankmenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController {
    private final BankMenu bankMenu;
    private final int[] prevPageButton = new int[]{1,1};
    private final int[] nextPageButton = new int[]{7,1};
    private final int[] accountsStart = new int[]{2,1};
    private final int accountsLength = 5;
    private final int accountsWidth = 1;
    private final int accountsSize = accountsWidth * accountsLength;
    private final List<int[]> accounts;
    private Map<Integer, Map<List<Integer>, AccountEntity>> pageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> pageNumbers = new ConcurrentHashMap<>();

    public MainMenuController(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
        this.accounts = bankMenu.rectangleArea(accountsStart, accountsWidth, accountsLength);
    }
    
    public void openMainMenu(Player player, Inventory inv) {
        pageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            bankMenu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        bankMenu.fill(inv, bankMenu.getBackgroundItems().get(Material.ORANGE_STAINED_GLASS_PANE));
        buttons(inv);
        bankMenu.rectangleAreaLoading(inv, accountsStart, accountsWidth, accountsLength);
        Scheduler.runAsync((task) -> {
            fetchAccounts(player);
            displayAccounts(player, inv);
        });
    }

    public void nextPage(Player player, Inventory inv) {
        pageNumbers.get(player)[0]++;
        if(pageData.containsKey(pageNumbers.get(player)[0])) {
            displayAccounts(player, inv); 
        } else {
            pageNumbers.get(player)[0]--;
        }     
    }

    public void prevPage(Player player, Inventory inv) {
        pageNumbers.get(player)[0]--;
        if(pageNumbers.get(player)[0] > 0) {
            displayAccounts(player, inv);
        } else {
            pageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        pageNumbers.remove(player);
    }

    public boolean unlockAccount(AccountEntity account, Player player) {
        if(account.isUnlocked()) {
            return true;
        } else {
            boolean successfulWithdrawal = bankMenu.getCurrencyService().withdrawPlayerPurse(player, account.getUnlockCurrencyType(), account.getUnlockCost());
            if(!successfulWithdrawal) {
                bankMenu.getLogger().sendPlayerErrorMessage(player, "Not enough currency to unlock this account.");
                return false;
            }
            account.setUnlocked(true);
            boolean successfulUnlock = bankMenu.getBankService().updateAccount(account);
            if(!successfulUnlock) {
                bankMenu.getLogger().sendPlayerErrorMessage(player, "Could not unlock the account.");
                bankMenu.getCurrencyService().depositPlayerPurse(player, account.getUnlockCurrencyType(), account.getUnlockCost());
                return false;
            }
            bankMenu.getLogger().sendPlayerNotificationMessage(player, String.format("Sucessfully unlocked account for %.2f %s", account.getUnlockCost(), account.getUnlockCurrencyType()));
            bankMenu.getSoundManager().playTransactionSound(player);
            return true;
        }
    }

    private void fetchAccounts(Player player) {
        pageData.clear();
        List<AccountEntity> accounts = bankMenu.getBankService().getPlayerAccountsByBank(bankMenu.getBankName(), player);
        if(accounts.isEmpty()) {
            return;
        }
        Queue<AccountEntity> temp = new ArrayDeque<>();
        temp.addAll(accounts);
        int maxPages = (int) Math.ceil((double) accounts.size() / (double) accountsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, AccountEntity> options = new LinkedHashMap<>();
            for (int[] coords : this.accounts) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayAccounts(Player player, Inventory inv) {
        Map<List<Integer>, AccountEntity> options = pageData.getOrDefault(pageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, AccountEntity> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            AccountEntity account = entry.getValue();
            if(account == null) {
                bankMenu.setItem(inv, coords, bankMenu.getUnavailable());
            } else {
                ItemStack item = account.getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("BANK ACCOUNT", NamedTextColor.GOLD));
                    lore.add(Component.text("Interest Rate: ", NamedTextColor.BLUE).append(Component.text(account.getInterestRate() + "%", NamedTextColor.DARK_GREEN).append(Component.text(String.format(" every %s hour(s)", account.getBank().getInterestCooldown().toHours()), NamedTextColor.YELLOW))));
                    if(!account.isUnlocked()) {
                        lore.add(Component.text("Unlock cost: ", NamedTextColor.BLUE).append(Component.text(String.format("%.2f %s", account.getUnlockCost(), account.getUnlockCurrencyType(), NamedTextColor.DARK_GREEN))));
                    }
                    meta.lore(lore);
                }
                item.setItemMeta(meta);
                bankMenu.setItem(inv, coords, item);
            }
        }
    }

    private void buttons(Inventory inv) {
        bankMenu.setItem(inv, nextPageButton, bankMenu.getNextPage());
        bankMenu.setItem(inv, prevPageButton, bankMenu.getPrevPage());
    }
}
