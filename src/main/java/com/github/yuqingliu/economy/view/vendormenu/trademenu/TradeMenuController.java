package com.github.yuqingliu.economy.view.vendormenu.trademenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;

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
    protected final List<Integer> buyOptions = Arrays.asList(29,30,31,32,33);
    protected final List<Integer> sellOptions = Arrays.asList(38,39,40,41,42);
    protected final List<Integer> buttons = Arrays.asList(20,24);
    protected VendorItemEntity item;
    
    public TradeMenuController(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
    }   

    public void openTradeMenu(Inventory inv, VendorItemEntity item) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            vendorMenu.setCurrentMenu(MenuType.TradeMenu);
        }, Duration.ofMillis(50));
        vendorMenu.clear(inv);
        pagePtrs(inv);
        frame(inv);
        displayItem(inv);
        displayBuyOptions(inv);
        displaySellOptions(inv);
    }

    private void displayItem(Inventory inv) {
        inv.setItem(itemSlot, item.getIcon().clone());
    }

    private void displayBuyOptions(Inventory inv) {
        int[] quantities = new int[] {1, 16, 32, 64};
        for (int i = buy1; i < buyInventory; i++) {
            int index = i - buy1;
            ItemStack icon = new ItemStack(Material.LIME_STAINED_GLASS);
            icon.setAmount(quantities[index]);
            inv.setItem(i, icon);
        }
        ItemStack inventoryOption = new ItemStack(Material.CHEST);
        inv.setItem(buyInventory, inventoryOption);
    }

    private void displaySellOptions(Inventory inv) {
        int[] quantities = new int[] {1, 16, 32, 64};
        for (int i = sell1; i < sellInventory; i++) {
            int index = i - sell1;
            ItemStack icon = new ItemStack(Material.RED_STAINED_GLASS);
            icon.setAmount(quantities[index]);
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
            if(!options.contains(i) && !buttons.contains(i)) {
                inv.setItem(i, Placeholder);
            }
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
