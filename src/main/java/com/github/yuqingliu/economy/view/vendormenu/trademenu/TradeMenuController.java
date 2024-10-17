package com.github.yuqingliu.economy.view.vendormenu.trademenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;
import com.github.yuqingliu.economy.view.vendormenu.transactionmenu.CurrencyOption;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class TradeMenuController {
    private final VendorMenu vendorMenu;
    protected final int itemSlot = 13;
    protected final int buy1 = 29;
    protected final int buyInventory = 33;
    protected final int sell1 = 38;
    protected final int sellInventory = 42;
    protected final int prev = 20;
    protected final int exit = 24;
    protected final List<Integer> options = Arrays.asList(13,29,30,31,32,33,38,39,40,41,42);
    protected final List<Integer> border = Arrays.asList(3,4,5,12,14,21,22,23);
    protected final List<Integer> buyOptions = Arrays.asList(29,30,31,32,33);
    protected final List<Integer> sellOptions = Arrays.asList(38,39,40,41,42);
    protected final List<Integer> buttons = Arrays.asList(20,24);
    protected final int[] quantities = new int[] {1, 16, 32, 64};
    protected VendorItemEntity item;
    protected CurrencyOption currencyOption;
    
    public TradeMenuController(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
    }   

    public void openTradeMenu(Inventory inv, VendorItemEntity item, CurrencyOption currencyOption, Player player) {
        this.item = item;
        this.currencyOption = currencyOption;
        Scheduler.runLaterAsync((task) -> {
            vendorMenu.getPlayerMenuTypes().put(player, MenuType.TradeMenu);
        }, Duration.ofMillis(50));
        vendorMenu.clear(inv);
        frame(inv);
        border(inv);
        pagePtrs(inv);
        displayItem(inv);
        displayBuyOptions(inv);
        displaySellOptions(inv);
    }

    private void displayItem(Inventory inv) {
        inv.setItem(itemSlot, item.getIcon().clone());
    }

    private void displayBuyOptions(Inventory inv) {
        for (int i = buy1; i < buyInventory; i++) {
            int index = i - buy1;
            ItemStack icon = new ItemStack(Material.LIME_STAINED_GLASS);
            icon.setAmount(quantities[index]);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.displayName(Component.text("BUY: ", NamedTextColor.GOLD).append(Component.text(quantities[index] + "x", NamedTextColor.RED)));
            Component cost = Component.text("COST: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getBuyPrice(quantities[index]) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()));
            iconMeta.lore(Arrays.asList(cost));
            icon.setItemMeta(iconMeta);
            inv.setItem(i, icon);
        }
        ItemStack inventoryOption = new ItemStack(Material.CHEST);
        inv.setItem(buyInventory, inventoryOption);
    }

    private void displaySellOptions(Inventory inv) {
        for (int i = sell1; i < sellInventory; i++) {
            int index = i - sell1;
            ItemStack icon = new ItemStack(Material.RED_STAINED_GLASS);
            icon.setAmount(quantities[index]);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.displayName(Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(quantities[index] + "x", NamedTextColor.RED)));
            Component profit = Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getSellPrice(quantities[index]) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()));
            iconMeta.lore(Arrays.asList(profit));
            icon.setItemMeta(iconMeta);
            inv.setItem(i, icon);
        }
        ItemStack inventoryOption = new ItemStack(Material.CHEST);
        inv.setItem(sellInventory, inventoryOption);
    }

    private void frame(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = Placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(meta);
        for (int i = 0; i < vendorMenu.getInventorySize(); i++) {
            inv.setItem(i, Placeholder);
        }
    }

    private void border(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = Placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(meta);
        for (int i : border) {
            inv.setItem(i, Placeholder);
        }
    }

    private void pagePtrs(Inventory inv) {
        ItemStack prev = new ItemStack(Material.GREEN_WOOL);
        ItemMeta prevmeta = prev.getItemMeta();
        if(prevmeta != null) {
            prevmeta.displayName(Component.text("Items", NamedTextColor.GRAY));
        }
        prev.setItemMeta(prevmeta);
        inv.setItem(this.prev, prev);

        ItemStack exit = new ItemStack(Material.RED_WOOL);
        ItemMeta emeta = exit.getItemMeta();
        if(emeta != null) {
            emeta.displayName(Component.text("Exit", NamedTextColor.RED));
        }
        exit.setItemMeta(emeta);
        inv.setItem(this.exit, exit);
    }
}
