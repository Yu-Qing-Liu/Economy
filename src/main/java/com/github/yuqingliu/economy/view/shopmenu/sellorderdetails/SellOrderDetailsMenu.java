package com.github.yuqingliu.economy.view.shopmenu.sellorderdetails;

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
public class SellOrderDetailsMenu implements Listener {
    private final ShopMenu shopMenu;
    private final SellOrderDetailsMenuController controller;

    public SellOrderDetailsMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new SellOrderDetailsMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.SellOrderDetailsMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(slot == controller.getPrev()) {
                shopMenu.getSellOrdersMenu().getController().openSellOrdersMenu(clickedInventory, player);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
            if(slot == controller.getCancelOrder()) {
                controller.cancelOrder(clickedInventory, player);
            }
            if(slot == controller.getClaimOrder()) {
                controller.claimOrder(clickedInventory, player);
            }
        }
    }
}
