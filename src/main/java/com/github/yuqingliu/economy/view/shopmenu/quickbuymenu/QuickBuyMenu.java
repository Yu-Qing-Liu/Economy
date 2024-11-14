package com.github.yuqingliu.economy.view.shopmenu.quickbuymenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;

@Getter
public class QuickBuyMenu implements Listener {
    private final ShopMenu shopMenu;
    private final QuickBuyMenuController controller;

    public QuickBuyMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new QuickBuyMenuController(shopMenu);
        shopMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(shopMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.QuickBuyMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = shopMenu.toCoords(event.getSlot());
            if(shopMenu.isUnavailable(currentItem)) {
                return;
            }
            if(shopMenu.rectangleContains(slot, controller.getBuyOptions())) {
                int index = shopMenu.rectangleIndex(slot, controller.getBuyOptions());
                int amount = controller.getQuantities()[index];
                controller.quickBuy(amount, player);
                return;
            }
            if(Arrays.equals(slot, controller.getBuyInventoryButton())) {
                int amount = shopMenu.getPluginManager().getInventoryManager().countAvailableInventorySpace(player, controller.getItem().getIcon().getType());
                controller.quickBuy(amount, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                controller.onClose(player);
                shopMenu.getOrderMenu().getController().openOrderMenu(clickedInventory, controller.getItem(), player);
                return;
            }
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(shopMenu.getDisplayName())) {
            controller.onClose((Player) event.getPlayer());
        }
    }
}
