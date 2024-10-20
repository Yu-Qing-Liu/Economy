package com.github.yuqingliu.economy.view.bankmenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final int length = 5;
    private final int prevPagePtr = 10;
    private final int nextPagePtr = 16;
    private Material voidOption = Material.GLASS_PANE;
    private final List<Integer> options = Arrays.asList(11,12,13,14,15);
    private final List<Integer> buttons = Arrays.asList(10,16);
    private Map<Integer, AccountEntity[]> pageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> pageNumbers = new ConcurrentHashMap<>();

    public MainMenuController(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
    }
    
    public void openMainMenu(Player player, Inventory inv) {
        pageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            bankMenu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        bankMenu.clear(inv);
        frame(inv);
        pagePtrs(inv);
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
        int maxPages = (int) Math.ceil((double) accounts.size() / (double) length);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            AccountEntity[] options = new AccountEntity[length];
            for (int j = 0; j < length; j++) {
                if(temp.isEmpty()) {
                    options[j] = null;
                } else {
                    options[j] = temp.poll();
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayAccounts(Player player, Inventory inv) {
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        AccountEntity[] options = pageData.getOrDefault(pageNumbers.get(player)[0], new AccountEntity[length]);
        int currentIndex = 0;
        for (int i : this.options) {
            AccountEntity account = options[currentIndex];
            if(account == null) {
                inv.setItem(i, Placeholder);
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
                inv.setItem(i, item);
            }
            currentIndex++;
        }
    }

    private void frame(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = Placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(meta);
        for (int i = 0; i < bankMenu.getInventorySize(); i++) {
            inv.setItem(i, Placeholder);
        }
    }

    private void pagePtrs(Inventory inv) {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nmeta = nextPage.getItemMeta();
        if(nmeta != null) {
            nmeta.displayName(Component.text("Next Page", NamedTextColor.AQUA));
        }
        nextPage.setItemMeta(nmeta);
        inv.setItem(nextPagePtr, nextPage);

        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta pmeta = prevPage.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Previous Page", NamedTextColor.AQUA));
        }
        prevPage.setItemMeta(pmeta);
        inv.setItem(prevPagePtr, prevPage);
    }
}
