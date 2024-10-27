package com.github.yuqingliu.economy.view.shopmenu.sellorderdetails;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class SellOrderDetailsMenuController {
    private final ShopMenu shopMenu;
    private final int itemSlot = 13;
    private final int prev = 11;
    private final int exit = 15;
    private final int cancelOrder = 29;
    private final int orderDetail = 31;
    private final int claimOrder = 33;
    private final List<Integer> options = Arrays.asList(13,29,30,31,32,33,38,39,40,41,42);
    private final List<Integer> border = Arrays.asList(3,4,5,11,12,14,15,19,20,21,22,23,24,25,28,30,32,34,37,38,39,40,41,42,43);
    private final List<Integer> sellOptions = Arrays.asList(29,30,31,32,33);
    private final List<Integer> buttons = Arrays.asList(20,24,29,31,33);
    private Map<Player, ShopOrderEntity> playersData = new ConcurrentHashMap<>();
    
    public SellOrderDetailsMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }   

    public void openSellOrderDetailsMenu(Inventory inv, ShopOrderEntity order, Player player) {
        playersData.put(player, order);
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.SellOrderDetailsMenu);
        }, Duration.ofMillis(50));
        reload(inv, player);
    }

    private void reload(Inventory inv, Player player) {
        shopMenu.clear(inv);
        frame(inv);
        border(inv);
        pagePtrs(inv);
        displayItem(inv, player);
        displayOrder(inv, player);
        buttons(inv, player);
    }

    public void cancelOrder(Inventory inv, Player player) {
        Scheduler.runAsync((task) -> {
            ShopOrderEntity order = playersData.get(player);
            double profit = order.getFilledQuantity() * order.getUnitPrice();
            order.setQuantity(order.getQuantity() - order.getFilledQuantity());
            order.setFilledQuantity(0);
            if(shopMenu.getShopService().updateOrder(order)) {
                shopMenu.getCurrencyService().depositPlayerPurse(player, order.getCurrencyType(), profit);
                playersData.put(player, order);
                int amount = order.getQuantity() - order.getFilledQuantity();
                if(shopMenu.getShopService().deleteOrder(order)) {
                    shopMenu.addItemToPlayer(player, order.getShopItem().getIcon().clone(), amount);
                    playersData.remove(player);
                    shopMenu.getSellOrdersMenu().getController().openSellOrdersMenu(inv, player);
                    return;
                } 
            }
            reload(inv, player);
        });
    }

    public void claimOrder(Inventory inv, Player player) {
        Scheduler.runAsync((task) -> {
            ShopOrderEntity order = playersData.get(player);
            double profit = order.getFilledQuantity() * order.getUnitPrice();
            order.setQuantity(order.getQuantity() - order.getFilledQuantity());
            order.setFilledQuantity(0);
            if(shopMenu.getShopService().updateOrder(order)) {
                shopMenu.getCurrencyService().depositPlayerPurse(player, order.getCurrencyType(), profit);
                playersData.put(player, order);
                if(order.getQuantity() == 0) {
                    if(shopMenu.getShopService().deleteOrder(order)) {
                        playersData.remove(player);
                        shopMenu.getSellOrdersMenu().getController().openSellOrdersMenu(inv, player);
                        return;
                    }
                } 
            }
            reload(inv, player);
        });
    }

    private void displayItem(Inventory inv, Player player) {
        ItemStack item = playersData.get(player).getShopItem().getIcon().clone();
        inv.setItem(itemSlot, item);
    }

    private void displayOrder(Inventory inv, Player player) {
        ShopOrderEntity order = playersData.get(player);
        ItemStack orderIcon = new ItemStack(Material.CREEPER_BANNER_PATTERN);
        ItemMeta meta = orderIcon.getItemMeta();
        if(meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.displayName(Component.text("SELL ORDER", NamedTextColor.GOLD));
            Component nameComponent = Component.text(order.getItemName(), NamedTextColor.AQUA);
            Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD));
            Component priceComponent = Component.text("Unit Buy Price: ", NamedTextColor.BLUE).append(Component.text(order.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
            Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() + "x", NamedTextColor.GREEN));
            Component quantityBoughtComponent = Component.text("Quantity Bought: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x", NamedTextColor.GREEN));
            meta.lore(Arrays.asList(nameComponent, currencyComponent, priceComponent, quantityComponent, quantityBoughtComponent));
        }
        orderIcon.setItemMeta(meta);
        inv.setItem(orderDetail, orderIcon);
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

    private void buttons(Inventory inv, Player player) {
        ShopOrderEntity order = playersData.get(player);
        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if(cancelMeta != null) {
            double refund = order.getFilledQuantity() * order.getUnitPrice();
            cancelMeta.displayName(Component.text("Cancel Order", NamedTextColor.RED));
            cancelMeta.lore(Arrays.asList(
                Component.text("Refund: ", NamedTextColor.BLUE).append(Component.text(refund + "$ ", NamedTextColor.DARK_GREEN).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD))),
                Component.text("Return: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() - order.getFilledQuantity() + "x ", NamedTextColor.DARK_GREEN).append(Component.text("items", NamedTextColor.GOLD)))
            ));
        }
        cancelButton.setItemMeta(cancelMeta);
        inv.setItem(cancelOrder, cancelButton);

        ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        if(confirmMeta != null) {
            double profit = order.getFilledQuantity() * order.getUnitPrice(); 
            confirmMeta.displayName(Component.text("Claim Order", NamedTextColor.GREEN));
            confirmMeta.lore(Arrays.asList(
                Component.text("Collect: ", NamedTextColor.BLUE).append(Component.text(profit + "$ ", NamedTextColor.DARK_GREEN).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD)))
            ));
        }
        confirmButton.setItemMeta(confirmMeta);
        inv.setItem(claimOrder, confirmButton);
    }

    private void pagePtrs(Inventory inv) {
        ItemStack prev = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta prevmeta = prev.getItemMeta();
        if(prevmeta != null) {
            prevmeta.displayName(Component.text("Orders", NamedTextColor.GRAY));
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
