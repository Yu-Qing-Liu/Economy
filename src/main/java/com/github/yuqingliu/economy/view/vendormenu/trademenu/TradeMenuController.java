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
    private final int itemSlot = 13;
    private final int buy1 = 29;
    private final int buyInventory = 33;
    private final int sell1 = 38;
    private final int sellInventory = 42;
    private final int prev = 20;
    private final int exit = 24;
    private final List<Integer> options = Arrays.asList(13,29,30,31,32,33,38,39,40,41,42);
    private final List<Integer> border = Arrays.asList(3,4,5,12,14,21,22,23);
    private final List<Integer> buyOptions = Arrays.asList(29,30,31,32,33);
    private final List<Integer> sellOptions = Arrays.asList(38,39,40,41,42);
    private final List<Integer> buttons = Arrays.asList(20,24);
    private final int[] quantities = new int[] {1, 16, 32, 64};
    private VendorItemEntity item;
    private CurrencyOption currencyOption;
    
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

    public void buy(Player player, int slotAmount) {
        Scheduler.runAsync((task) -> {
            double cost = slotAmount * currencyOption.getBuyPrice();
            boolean successfulWithdrawal = vendorMenu.getCurrencyService().withdrawPlayerPurse(player, currencyOption.getCurrencyName(), cost);
            if(!successfulWithdrawal) {
                vendorMenu.getLogger().sendPlayerErrorMessage(player, "Not enough currency.");
                return;
            }
            vendorMenu.addItemToPlayer(player, item.getIcon().clone(), slotAmount);
            vendorMenu.getLogger().sendPlayerNotificationMessage(player, String.format("Bought %d item(s) for %.2f %s", slotAmount, cost, currencyOption.getCurrencyName()));
            vendorMenu.getSoundManager().playTransactionSound(player);
        });
    }

    public void sell(Player player, int slotAmount) {
        Scheduler.runAsync((task) -> {
            int amount = Math.min(vendorMenu.countItemToPlayer(player, item.getIcon().clone()), slotAmount);
            double profit = amount * currencyOption.getSellPrice();
            boolean sucessfulItemRemoval = vendorMenu.removeItemToPlayer(player, item.getIcon().clone(), amount);
            if(!sucessfulItemRemoval) {
                vendorMenu.getLogger().sendPlayerErrorMessage(player, "Not enough items to sell.");
                return;
            }
            boolean sucessfulDeposit = vendorMenu.getCurrencyService().depositPlayerPurse(player, currencyOption.getCurrencyName(), profit);
            if(!sucessfulDeposit) {
                vendorMenu.getLogger().sendPlayerErrorMessage(player, "Could not sell item(s)");
                vendorMenu.addItemToPlayer(player, item.getIcon().clone(), amount);
            }
            vendorMenu.getLogger().sendPlayerNotificationMessage(player, String.format("Sold %d item(s) for %.2f %s", amount, profit, currencyOption.getCurrencyName()));
            vendorMenu.getSoundManager().playTransactionSound(player);
        });
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
