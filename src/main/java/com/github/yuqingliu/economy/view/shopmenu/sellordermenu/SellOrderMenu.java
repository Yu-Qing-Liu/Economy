package com.github.yuqingliu.economy.view.shopmenu.sellordermenu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;

@Getter
public class SellOrderMenu implements Listener {
    private final ShopMenu shopMenu;
    private final SellOrderMenuController controller;

    public SellOrderMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new SellOrderMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.SellOrderMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(slot == controller.getPrev()) {
                controller.getRefreshTask().cancel();
                shopMenu.getOrderMenu().getController().openOrderMenu(clickedInventory, controller.getItem(), player);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
            if(slot == controller.getSetCurrencyType()) {
                controller.setCurrencyType(clickedInventory, player);
            }
            if(slot == controller.getSetQuantity()) {
                controller.setQuantity(clickedInventory, player);
            }
            if(slot == controller.getSetUnitPrice()) {
                controller.setUnitPrice(clickedInventory, player);
            }
            if(slot == controller.getConfirm()) {
                if(currentItem.getType() != controller.getVoidOption()) {
                    controller.confirmOrder(clickedInventory, player);
                    controller.getRefreshTask().cancel();
                }
            }
        }
    }
}
