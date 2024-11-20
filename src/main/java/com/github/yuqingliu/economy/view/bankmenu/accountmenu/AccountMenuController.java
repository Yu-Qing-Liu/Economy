package com.github.yuqingliu.economy.view.bankmenu.accountmenu;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class AccountMenuController extends AbstractPlayerInventoryController<BankMenu> {
    private final int[] prevMenuButton = new int[]{1,1};
    private final int[] exitMenuButton = new int[]{2,1};
    private final int[] accountInfo = new int[]{3,1};
    private final int[] depositButton = new int[]{5,1};
    private final int[] withdrawButton = new int[]{7,1};
    AccountEntity account;

    public AccountMenuController(Player player, Inventory inventory, BankMenu bankMenu) {
        super(player, inventory, bankMenu);
    }
    
    public void openMenu(AccountEntity account) {
        this.account = account;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.AccountMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.ORANGE_STAINED_GLASS_PANE));
        accountDetails();
        buttons();
    }

    private void accountDetails() {
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
        setItem(accountInfo, icon);
    }

    private void buttons() {
        setItem(prevMenuButton, getPrevMenuIcon());       
        setItem(exitMenuButton, getExitMenuIcon());       
        setItem(depositButton, createSlotItem(Material.ENDER_CHEST, Component.text("Deposit", NamedTextColor.GOLD)));
        setItem(withdrawButton, createSlotItem(Material.CHEST, Component.text("Withdraw", NamedTextColor.RED)));
    }
}
