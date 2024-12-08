package com.github.yuqingliu.economy.view.shopmenu.sellorderdetails;

import java.util.Arrays;

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
        shopMenu.getPluginManager().getEventManager().registerEvent(this);
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
            int[] slot = shopMenu.toCoords(event.getSlot());
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getSellOrdersMenu().getController().openSellOrdersMenu(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getCancelOrderButton())) {
                controller.cancelOrder(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getClaimOrderButton())) {
                controller.claimOrder(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getRefreshButton())) {
                controller.reload(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
                clickedInventory.close();
            }
        }
    }
}
