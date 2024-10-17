package com.github.yuqingliu.economy.view.shopmenu.ordersmenu;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;

@Getter
public class OrdersMenu implements Listener {
    private final ShopMenu shopMenu;
    private final OrdersMenuController controller;

    public OrdersMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new OrdersMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.OrdersMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getBuyOrders().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getBuyOrders().get(0);
                PlayerBuyOrdersData data = controller.getPlayerBuyOrdersData().get(player);
                Map<Integer, ShopOrderEntity[]> pageData = controller.getPlayerBuyOrdersData().get(player).getBuyOrdersPageData();
                if(pageData != null && pageData.containsKey(data.getPageNumber()[0])) {
                    shopMenu.getBuyOrderDetailsMenu().getController().openBuyOrderDetailsMenu(clickedInventory, pageData.get(data.getPageNumber()[0])[index], player);
                }
            }
            if(controller.getSellOrders().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getSellOrders().get(0);
                // if(controller.getSellPageData() != null && controller.getSellPageData().containsKey(controller.getSellPageNumbers().get(player)[0])) {
                //     shopMenu.getQuickBuyMenu().getController().openQuickBuyMenu(clickedInventory, controller.getItem(), controller.getSellPageData().get(controller.getSellPageNumbers().get(player)[0])[index], player);
                // }
            }
            if(slot == controller.getNextBuyOrders()) {
                controller.nextBuyOrdersPage(clickedInventory, player);
            }
            if(slot == controller.getPrevBuyOrders()) {
                controller.prevBuyOrdersPage(clickedInventory, player);
            }
            if(slot == controller.getNextSellOrders()) {
                controller.nextSellOrdersPage(clickedInventory, player);
            }
            if(slot == controller.getPrevSellOrders()) {
                controller.prevSellOrdersPage(clickedInventory, player);
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
