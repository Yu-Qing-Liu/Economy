package com.github.yuqingliu.economy.view.shopmenu.quicksellmenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderOption;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class QuickSellMenuController {
    private final ShopMenu shopMenu;
    private final int itemSlot = 13;
    private final int sell1 = 29;
    private final int sellInventory = 33;
    private final int prev = 20;
    private final int exit = 24;
    private final List<Integer> options = Arrays.asList(13,29,30,31,32,33,38,39,40,41,42);
    private final List<Integer> border = Arrays.asList(3,4,5,12,14,21,22,23,38,39,40,41,42);
    private final List<Integer> sellOptions = Arrays.asList(29,30,31,32);
    private final List<Integer> buttons = Arrays.asList(20,24);
    private final int[] quantities = new int[] {1, 16, 32, 64};
    private ShopItemEntity item;
    private OrderOption orderOption;
    
    public QuickSellMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }   

    public void openQuickSellMenu(Inventory inv, ShopItemEntity item, OrderOption orderOption, Player player) {
        this.item = item;
        this.orderOption = orderOption;
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.QuickSellMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
        frame(inv);
        border(inv);
        pagePtrs(inv);
        displayItem(inv);
        displaySellOptions(inv);
    }

    public void quickSell(int amount, Player player) {
        Scheduler.runAsync((task) -> {
            int required = amount;
            for(ShopOrderEntity order : orderOption.getOrders()) {
                int qty = order.getQuantity();
                if(qty > required) {
                    order.setFilledQuantity(required);
                    boolean sucessfulItemRemoval = shopMenu.removeItemToPlayer(player, item.getIcon().clone(), required);
                    if(!sucessfulItemRemoval) {
                        return;
                    }
                    boolean sucessfulOrderUpdate = shopMenu.getShopService().updateOrder(order);
                    if(!sucessfulOrderUpdate) {
                        shopMenu.addItemToPlayer(player, item.getIcon().clone(), required);
                        return;
                    }
                    boolean sucessfulDeposit = shopMenu.getCurrencyService().depositPlayerPurse(player, order.getCurrencyType(), required * order.getUnitPrice());
                    if(!sucessfulDeposit) {
                        shopMenu.addItemToPlayer(player, item.getIcon().clone(), required);
                        return;
                    }
                    break;
                } else {
                    order.setFilledQuantity(qty);
                    boolean sucessfulItemRemoval = shopMenu.removeItemToPlayer(player, item.getIcon().clone(), required);
                    if(!sucessfulItemRemoval) {
                        return;
                    }
                    boolean sucessfulOrderUpdate = shopMenu.getShopService().updateOrder(order);
                    if(!sucessfulOrderUpdate) {
                        shopMenu.addItemToPlayer(player, item.getIcon().clone(), required);
                        return;
                    }
                    boolean sucessfulDeposit = shopMenu.getCurrencyService().depositPlayerPurse(player, order.getCurrencyType(), required * order.getUnitPrice());
                    if(!sucessfulDeposit) {
                        shopMenu.addItemToPlayer(player, item.getIcon().clone(), required);
                        return;
                    }
                    required -= qty;
                }
            }
        });
    }

    private void displayItem(Inventory inv) {
        inv.setItem(itemSlot, item.getIcon().clone());
    }

    private void displaySellOptions(Inventory inv) {
        for (int i = sell1; i < sellInventory; i++) {
            int index = i - sell1;
            ItemStack icon = new ItemStack(Material.RED_STAINED_GLASS);
            icon.setAmount(quantities[index]);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.displayName(Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(quantities[index] + "x", NamedTextColor.RED)));
            double profit = 0;
            int qty = quantities[index];
            for(ShopOrderEntity order : orderOption.getOrders()) {
                int amount = order.getQuantity() - order.getFilledQuantity();
                if(amount > qty) {
                    profit = qty * order.getUnitPrice();
                    break;
                } else {
                    qty -= amount;
                    profit += amount * order.getUnitPrice();
                }
            }
            Component costComponent = Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(profit +"$ ", NamedTextColor.DARK_GREEN).append(orderOption.getIcon().displayName()));
            iconMeta.lore(Arrays.asList(costComponent));
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
        for (int i = 0; i < shopMenu.getInventorySize(); i++) {
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
