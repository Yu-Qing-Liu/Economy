package com.github.yuqingliu.economy.view.pursemenu.mainmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu.MenuType;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final PurseMenu purseMenu;
    private final MainMenuController controller;
    
    public MainMenu(PurseMenu purseMenu) {
        this.purseMenu = purseMenu;
        this.controller = new MainMenuController(purseMenu);
        purseMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(purseMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(purseMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = purseMenu.toCoords(event.getSlot());
            if(purseMenu.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getNextPageButton())) {
                controller.nextPage(player, clickedInventory);
                return;
            } 
            if(Arrays.equals(slot, controller.getPrevPageButton())) {
                controller.prevPage(player, clickedInventory);
                return;
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(purseMenu.getDisplayName())) {
            controller.onClose((Player) event.getPlayer());
        }
    }
}
