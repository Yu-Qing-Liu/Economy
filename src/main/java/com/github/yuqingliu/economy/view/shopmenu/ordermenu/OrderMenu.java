package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

import java.util.Arrays;

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
public class OrderMenu implements Listener {
    private final ShopMenu shopMenu;
    private final OrderMenuController controller;

    public OrderMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new OrderMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.OrderMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = shopMenu.toCoords(event.getSlot());
            if(shopMenu.isUnavailable(currentItem)) {
                return;
            }
            if(shopMenu.rectangleContains(slot, controller.getBuyOrders())) {
                int pageNumber = controller.getBuyPageNumbers().get(player)[0];
                OrderOption orderOption = controller.getBuyPageData().get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                shopMenu.getQuickSellMenu().getController().openQuickSellMenu(clickedInventory, controller.getItem(), orderOption, player);
                return;
            }
            if(shopMenu.rectangleContains(slot, controller.getSellOrders())) {
                int pageNumber = controller.getSellPageNumbers().get(player)[0];
                OrderOption orderOption = controller.getSellPageData().get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                shopMenu.getQuickBuyMenu().getController().openQuickBuyMenu(clickedInventory, controller.getItem(), orderOption, player);
                return;
            }
            if(Arrays.equals(slot, controller.getCreateBuyOrderButton())) {
                shopMenu.getBuyOrderMenu().getController().openBuyOrderMenu(clickedInventory, controller.getItem(), player);
                return;
            }
            if(Arrays.equals(slot, controller.getCreateSellOrderButton())) {
                shopMenu.getSellOrderMenu().getController().openSellOrderMenu(clickedInventory, controller.getItem(), player);
                return;
            }
            if(Arrays.equals(slot, controller.getRefreshButton())) {
                controller.reload(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getMainMenu().getController().openMainMenu(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getNextBuyOrdersButton())) {
                controller.nextBuyPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevBuyOrdersButton())) {
                controller.prevBuyPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getNextSellOrdersButton())) {
                controller.nextSellPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevSellOrdersButton())) {
                controller.prevSellPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
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
