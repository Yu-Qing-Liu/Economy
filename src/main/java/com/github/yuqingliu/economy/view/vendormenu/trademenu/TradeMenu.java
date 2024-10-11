package com.github.yuqingliu.economy.view.vendormenu.trademenu;

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
import com.github.yuqingliu.economy.view.vendormenu.itemmenu.ItemMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class TradeMenu extends VendorMenu implements Listener {
    private final TradeMenuController controller;

    public TradeMenu(EventManager eventManager, Component displayName, VendorService vendorService, CurrencyService currencyService) {
        super(eventManager, displayName, vendorService, currencyService);
        this.controller = new TradeMenuController(eventManager, displayName, vendorService, currencyService);
        eventManager.registerEvent(this);
        currentMenu = MenuType.TradeMenu;
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

        if(currentMenu == MenuType.TradeMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(slot == controller.getPrev()) {
                new ItemMenu(eventManager, displayName, vendorService, currencyService).getController().openItemMenu(clickedInventory, controller.getItem().getVendorSection());
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(displayName)) {
            eventManager.unregisterEvent(this.getClass().getSimpleName());
        }
    }
}
