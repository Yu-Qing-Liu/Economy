package com.github.yuqingliu.economy.view.shopmenu.buyordermenu;

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
public class BuyOrderMenu implements Listener {
    private final ShopMenu shopMenu;
    private final BuyOrderMenuController controller;

    public BuyOrderMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new BuyOrderMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.BuyOrderMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = shopMenu.toCoords(event.getSlot());
            if(shopMenu.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                controller.onClose(player);
                shopMenu.getOrderMenu().getController().openOrderMenu(clickedInventory, controller.getItem(), player);
                return;
            }
            if(Arrays.equals(slot, controller.getSetCurrencyTypeButton())) {
                controller.setCurrencyType(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getSetQuantityButton())) {
                controller.setQuantity(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getSetPriceButton())) {
                controller.setUnitPrice(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getConfirmOrderButton())) {
                controller.confirmOrder(clickedInventory, player);
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
