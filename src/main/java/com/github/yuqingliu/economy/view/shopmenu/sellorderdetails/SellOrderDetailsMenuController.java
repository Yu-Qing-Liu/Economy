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
    private final int[] itemSlot = new int[]{4,1};
    private final int[] orderInfo = new int[]{4,3};
    private final int[] prevMenuButton = new int[]{2,1};
    private final int[] exitMenuButton = new int[]{6,1};
    private final int[] cancelOrderButton = new int[]{2,3};
    private final int[] claimOrderButton = new int[]{6,3};
    private final int[] refreshButton = new int[]{4,4};
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

    public void reload(Inventory inv, Player player) {
        shopMenu.fill(inv, shopMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        border(inv);
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
        shopMenu.setItem(inv, itemSlot, item);
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
        shopMenu.setItem(inv, orderInfo, orderIcon);
    }

    private void border(Inventory inv) {
        ItemStack borderItem = shopMenu.createSlotItem(Material.BLACK_STAINED_GLASS_PANE, shopMenu.getUnavailableComponent());
        shopMenu.fillRectangleArea(inv, new int[]{1,2}, 3, 7, borderItem);
        shopMenu.fillRectangleArea(inv, new int[]{3,0}, 2, 3, borderItem);
    }

    private void buttons(Inventory inv, Player player) {
        ShopOrderEntity order = playersData.get(player);
        shopMenu.setItem(inv, prevMenuButton, shopMenu.getPrevMenu());
        shopMenu.setItem(inv, exitMenuButton, shopMenu.getExitMenu());
        shopMenu.setItem(inv, refreshButton, shopMenu.createSlotItem(Material.YELLOW_WOOL, Component.text("Refresh", NamedTextColor.YELLOW)));
        double refund = (order.getQuantity() - order.getFilledQuantity()) * order.getUnitPrice(); 
        double profit = order.getFilledQuantity() * order.getUnitPrice(); 
        List<Component> cancelLore = Arrays.asList(
            Component.text("Refund: ", NamedTextColor.BLUE).append(Component.text(refund + "$ ", NamedTextColor.DARK_GREEN).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD))),
            Component.text("Return: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() - order.getFilledQuantity() + "x ", NamedTextColor.DARK_GREEN).append(Component.text("items", NamedTextColor.GOLD)))
        );
        ItemStack cancelButton = shopMenu.createSlotItem(Material.RED_CONCRETE, Component.text("Cancel Order", NamedTextColor.RED), cancelLore);
        shopMenu.setItem(inv, cancelOrderButton, cancelButton);
        ItemStack confirmButton = shopMenu.createSlotItem(Material.LIME_CONCRETE, Component.text("Claim Order"), Component.text("Collect: ", NamedTextColor.BLUE).append(Component.text(profit + "$ ", NamedTextColor.DARK_GREEN).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD))));
        shopMenu.setItem(inv, claimOrderButton, confirmButton);
    }
}
