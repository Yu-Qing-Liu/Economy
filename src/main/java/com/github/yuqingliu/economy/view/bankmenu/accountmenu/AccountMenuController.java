package com.github.yuqingliu.economy.view.bankmenu.accountmenu;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class AccountMenuController {
    private final BankMenu bankMenu;
    private final int[] prevMenuButton = new int[]{1,1};
    private final int[] exitMenuButton = new int[]{2,1};
    private final int[] accountInfo = new int[]{3,1};
    private final int[] depositButton = new int[]{5,1};
    private final int[] withdrawButton = new int[]{7,1};
    private Map<Player, AccountEntity> accounts = new ConcurrentHashMap<>();

    public AccountMenuController(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
    }
    
    public void openAccountMenu(Player player, Inventory inv, AccountEntity account) {
        this.accounts.put(player, account);
        Scheduler.runLaterAsync((task) -> {
            bankMenu.getPlayerMenuTypes().put(player, MenuType.AccountMenu);
        }, Duration.ofMillis(50));
        bankMenu.fill(inv, bankMenu.getBackgroundItems().get(Material.ORANGE_STAINED_GLASS_PANE));
        accountDetails(inv, player);
        buttons(inv);
    }

    public void onClose(Player player) {
        accounts.remove(player);
    }

    private void accountDetails(Inventory inv, Player player) {
        AccountEntity account = accounts.get(player);
        ItemStack icon = account.getIcon().clone();
        ItemMeta meta = icon.getItemMeta();
        if(meta != null) {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Interest Rate: ", NamedTextColor.DARK_AQUA).append(Component.text(account.getInterestRate() + "%", NamedTextColor.DARK_GREEN)));
            for(CurrencyEntity currency : account.getCurrencies()) {
                Component currencyInfo = Component.text(String.format("%s: ", currency.getCurrencyName()), NamedTextColor.BLUE).append(Component.text(currency.getAmount(), NamedTextColor.GOLD));
                lore.add(currencyInfo);
            }
            meta.lore(lore);
        }
        icon.setItemMeta(meta);
        bankMenu.setItem(inv, accountInfo, icon);
    }

    private void buttons(Inventory inv) {
        bankMenu.setItem(inv, prevMenuButton, bankMenu.getPrevMenu());       
        bankMenu.setItem(inv, exitMenuButton, bankMenu.getExitMenu());       
        bankMenu.setItem(inv, depositButton, bankMenu.createSlotItem(Material.ENDER_CHEST, Component.text("Deposit", NamedTextColor.GOLD)));
        bankMenu.setItem(inv, withdrawButton, bankMenu.createSlotItem(Material.CHEST, Component.text("Withdraw", NamedTextColor.RED)));
    }
}
