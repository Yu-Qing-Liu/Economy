package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

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
public class TransactionMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final TransactionMenuController controller;

    public TransactionMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.controller = new TransactionMenuController(vendorMenu);
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

        if(vendorMenu.getPlayerMenuTypes().get(player) == MenuType.TransactionMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = vendorMenu.toCoords(event.getSlot());
            if(vendorMenu.isUnavailable(currentItem)) {
                return;
            }
            if(vendorMenu.rectangleContains(slot, controller.getCurrencyOptions())) {
                int pageNumber = controller.getPageNumbers().get(player)[0];
                CurrencyOption currencyOption = controller.getPageData().get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                vendorMenu.getTradeMenu().getController().openTradeMenu(clickedInventory, controller.getItem(), currencyOption, player);
                return;
            }
            if(Arrays.equals(slot, controller.getNextOptionsButton())) {
                controller.nextPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevOptionsButton())) {
                controller.prevPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                vendorMenu.getMainMenu().getController().openMainMenu(clickedInventory, player);
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
