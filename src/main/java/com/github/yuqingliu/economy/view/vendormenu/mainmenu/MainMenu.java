package com.github.yuqingliu.economy.view.vendormenu.mainmenu;

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
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getOptions().get(0);
                if(controller.getPageData() != null && controller.getPageData().containsKey(controller.getPageNumbers().get(player)[0])) {
                    vendorMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getPageData().get(controller.getPageNumbers().get(player)[0])[index], player);
                }
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(clickedInventory, player);
            } 
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(clickedInventory, player);
            }
            if(slot == controller.getExit()) {
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
