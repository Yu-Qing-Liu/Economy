package com.github.yuqingliu.economy.view.vendormenu.itemmenu;

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
public class ItemMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final ItemMenuController controller;

    public ItemMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.controller = new ItemMenuController(vendorMenu);
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

        if(vendorMenu.getCurrentMenu() == MenuType.ItemMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                // Open currency options menu
                int index = slot % 7 - 3;
                if(controller.getPageData() != null && controller.getPageData().containsKey(controller.getPageNumber())) {
                    vendorMenu.getTransactionMenu().getController().openTransactionMenu(clickedInventory, controller.getPageData().get(controller.getPageNumber())[index]);
                }
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(clickedInventory);
            }
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(clickedInventory);
            }
            if(slot == controller.getPrev()) {
                vendorMenu.getMainMenu().getController().openMainMenu(clickedInventory);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(vendorMenu.getDisplayName())) {
            controller.onClose();
        }
    }
}
