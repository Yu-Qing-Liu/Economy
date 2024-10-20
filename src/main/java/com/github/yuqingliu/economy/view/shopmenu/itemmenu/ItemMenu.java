package com.github.yuqingliu.economy.view.shopmenu.itemmenu;

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
public class ItemMenu implements Listener {
    private final ShopMenu shopMenu;
    private final ItemMenuController controller;

    public ItemMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new ItemMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.ItemMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getOptions().get(0);
                if(controller.getPageData() != null && controller.getPageData().containsKey(controller.getPageNumbers().get(player)[0])) {
                    shopMenu.getOrderMenu().getController().openOrderMenu(clickedInventory, controller.getPageData().get(controller.getPageNumbers().get(player)[0])[index], player);
                }
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(clickedInventory, player);
            }
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(clickedInventory, player);
            }
            if(slot == controller.getPrev()) {
                shopMenu.getMainMenu().getController().openMainMenu(clickedInventory, player);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(shopMenu.getDisplayName())) {
            controller.onClose((Player) event.getPlayer());
        }
    }
}
