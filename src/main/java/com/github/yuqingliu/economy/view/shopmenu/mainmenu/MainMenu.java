package com.github.yuqingliu.economy.view.shopmenu.mainmenu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final ShopMenu shopMenu;
    private final MainMenuController controller;

    public MainMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new MainMenuController(shopMenu);
        shopMenu.getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(shopMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(shopMenu.getCurrentMenu() == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                // Open item menu
                int index = slot - controller.getOptions().get(0);
                if(controller.getPageData() != null && controller.getPageData().containsKey(controller.getPageNumber())) {
                    shopMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getPageData().get(controller.getPageNumber())[index]);
                }
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(clickedInventory);
            } 
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(clickedInventory);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(shopMenu.getDisplayName())) {
            controller.onClose();
        }
    }
}
