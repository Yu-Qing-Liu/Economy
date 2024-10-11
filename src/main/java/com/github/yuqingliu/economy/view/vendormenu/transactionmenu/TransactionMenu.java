package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.mainmenu.MainMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class TransactionMenu extends VendorMenu implements Listener {
    private final TransactionMenuController controller;

    public TransactionMenu(EventManager eventManager, Component displayName, VendorService vendorService, CurrencyService currencyService) {
        super(eventManager, displayName, vendorService, currencyService);
        this.controller = new TransactionMenuController(eventManager, displayName, vendorService, currencyService);
        eventManager.registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(displayName)) {
            return;
        }

        event.setCancelled(true);

        if(controller.getCurrentMenu() == MenuType.TransactionMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                // Open trade menu
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(clickedInventory);
            }
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(clickedInventory);
            }
            if(slot == controller.getPrev()) {
                new MainMenu(eventManager, displayName, vendorService).getController().openMainMenu(clickedInventory);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(displayName)) {
            controller.onClose();
            eventManager.unregisterEvent(this.getClass().getSimpleName());
        }
    }
}
