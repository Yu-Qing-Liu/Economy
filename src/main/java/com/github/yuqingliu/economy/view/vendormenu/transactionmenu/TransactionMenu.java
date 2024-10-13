package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

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

        if(vendorMenu.getCurrentMenu() == MenuType.TransactionMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getOptions().get(0);
                if(controller.getPageData() != null && controller.getPageData().containsKey(controller.getPageNumber())) {
                    vendorMenu.getTradeMenu().getController().openTradeMenu(clickedInventory, controller.getItem(), controller.getPageData().get(controller.getPageNumber())[index]);
                }
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(clickedInventory);
            }
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(clickedInventory);
            }
            if(slot == controller.getPrev()) {
                vendorMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getItem().getVendorSection());
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
