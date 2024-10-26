package com.github.yuqingliu.economy.view.vendormenu.mainmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final MainMenuController controller;

    public MainMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.controller = new MainMenuController(vendorMenu);
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

        if(vendorMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = vendorMenu.toCoords(event.getSlot());
            if(vendorMenu.isUnavailable(currentItem)) {
                return;
            }
            if(vendorMenu.rectangleContains(slot, controller.getSectionsOptions())) {
                controller.displayInitialItems(clickedInventory, player, slot);
                return;
            }
            if(vendorMenu.rectangleContains(slot, controller.getItemsOptions())) {
                int pageNumber = controller.getItemPageNumbers().get(player)[0];
                VendorItemEntity item = controller.getPageItemData().get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                vendorMenu.getTransactionMenu().getController().openTransactionMenu(clickedInventory, item, player);
                return;
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
