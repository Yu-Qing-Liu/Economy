package com.github.yuqingliu.economy.view.pursemenu.mainmenu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class MainMenu extends PurseMenu implements Listener {
    private final MainMenuController controller;
    
    public MainMenu(EventManager eventManager, Component displayName, CurrencyService currencyService) {
        super(eventManager, displayName, currencyService);
        this.controller = new MainMenuController(eventManager, displayName, currencyService);
        eventManager.registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(controller.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(controller.getCurrentMenu() == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot)) {
                // Open currency details
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(player, clickedInventory);
            } 
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(player, clickedInventory);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(displayName)) {
            controller.onClose();
        }
    }
}
