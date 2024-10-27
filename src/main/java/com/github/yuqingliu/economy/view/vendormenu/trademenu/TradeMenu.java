package com.github.yuqingliu.economy.view.vendormenu.trademenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;

import lombok.Getter;

@Getter
public class TradeMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final TradeMenuController controller;

    public TradeMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.controller = new TradeMenuController(vendorMenu);
        vendorMenu.getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(vendorMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(vendorMenu.getPlayerMenuTypes().get(player) == MenuType.TradeMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = vendorMenu.toCoords(event.getSlot());
            if(vendorMenu.isUnavailable(currentItem)) {
                return;
            }
            if(vendorMenu.rectangleContains(slot, controller.getBuyOptions())) {
                int index = vendorMenu.rectangleIndex(slot, controller.getBuyOptions());
                controller.buy(player, controller.getQuantities()[index]);
                return;
            }
            if(vendorMenu.rectangleContains(slot, controller.getSellOptions())) {
                int index = vendorMenu.rectangleIndex(slot, controller.getSellOptions());
                controller.sell(player, controller.getQuantities()[index]);
                return;
            }
            if(Arrays.equals(slot, controller.getBuyInventoryButton())) {
                int amount = vendorMenu.countAvailableInventorySpace(player, controller.getItem().getIcon().getType());
                controller.buy(player, amount);
            }
            if(Arrays.equals(slot, controller.getSellInventoryButton())) {
                int amount = vendorMenu.countItemFromPlayer(player, controller.getItem().getIcon());
                controller.sell(player, amount);
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                controller.onClose(player);
                vendorMenu.getTransactionMenu().getController().openTransactionMenu(clickedInventory, controller.getItem(), player);
                return;
            }
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(vendorMenu.getDisplayName())) {
            controller.onClose((Player) event.getPlayer());
        }
    }
}
