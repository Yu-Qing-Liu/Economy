package com.github.yuqingliu.economy.view.bankmenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.view.PageData;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;
import com.github.yuqingliu.economy.view.bankmenu.accountmenu.AccountMenuController;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController extends AbstractPlayerInventoryController<BankMenu> {
    private final int[] prevPageButton = new int[]{1,1};
    private final int[] nextPageButton = new int[]{7,1};
    private final int[] accountsStart = new int[]{2,1};
    private final int accountsLength = 5;
    private final int accountsWidth = 1;
    private final int accountsSize = accountsWidth * accountsLength;
    private final List<int[]> accounts;
    private final PageData<AccountEntity> pageData = new PageData<>();

    public MainMenuController(Player player, Inventory inventory, BankMenu bankMenu) {
        super(player, inventory, bankMenu);
        this.accounts = rectangleArea(accountsStart, accountsWidth, accountsLength);
    }
    
    public void openMenu() {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.ORANGE_STAINED_GLASS_PANE));
        buttons();
        rectangleAreaLoading(accountsStart, accountsWidth, accountsLength);
        Scheduler.runAsync((task) -> {
            fetchAccounts();
            displayAccounts();
        });
    }

    public void nextPage() {
        pageData.nextPage(() -> displayAccounts());
    }

    public void prevPage() {
        pageData.prevPage(() -> displayAccounts());
    }

    public void unlockAccount(int[] slot) {
        AccountEntity account = pageData.get(slot);
        if(!menu.getBankService().unlockAccount(account, player)) {
            return;
        }
        menu.getAccountMenu().getControllers().getPlayerInventoryController(player, new AccountMenuController(player, inventory, menu)).openMenu(account);
    }

    private void fetchAccounts() {
        pageData.clear();
        List<AccountEntity> accounts = menu.getBankService().getPlayerAccountsByBank(menu.getBankName(), player);
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

    private void displayAccounts() {
        Map<List<Integer>, AccountEntity> options = pageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, AccountEntity> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            AccountEntity account = entry.getValue();
            if(account == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack item = account.getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("BANK ACCOUNT", NamedTextColor.GOLD));
                    lore.add(Component.text("Interest Rate: ", NamedTextColor.BLUE).append(Component.text(account.getInterestRate() * 100 + "%", NamedTextColor.DARK_GREEN).append(Component.text(String.format(" every %s hour(s)", account.getBank().getInterestCooldown().toHours()), NamedTextColor.YELLOW))));
                    if(!account.isUnlocked()) {
                        lore.add(Component.text("Unlock cost: ", NamedTextColor.BLUE).append(Component.text(String.format("%.2f", account.getUnlockCost()), NamedTextColor.DARK_GREEN)).append(Component.text(String.format(" %s", account.getUnlockCurrencyType()), NamedTextColor.GOLD)));
                    }
                    meta.lore(lore);
                }
                item.setItemMeta(meta);
                setItem(coords, item);
            }
        }
    }

    private void buttons() {
        setItem(nextPageButton, getNextPageIcon());
        setItem(prevPageButton, getPrevPageIcon());
    }
}
