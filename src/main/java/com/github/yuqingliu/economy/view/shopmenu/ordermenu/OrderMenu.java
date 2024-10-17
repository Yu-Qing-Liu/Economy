package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.OrderMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getBuyOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getBuyOptions().get(0);
                if(controller.getBuyPageData() != null && controller.getBuyPageData().containsKey(controller.getBuyPageNumbers().get(player)[0])) {
                    shopMenu.getQuickSellMenu().getController().openQuickSellMenu(clickedInventory, controller.getItem(), controller.getBuyPageData().get(controller.getBuyPageNumbers().get(player)[0])[index], player);
                }
            }
            if(controller.getSellOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getSellOptions().get(0);
                if(controller.getSellPageData() != null && controller.getSellPageData().containsKey(controller.getSellPageNumbers().get(player)[0])) {
                    shopMenu.getQuickBuyMenu().getController().openQuickBuyMenu(clickedInventory, controller.getItem(), controller.getSellPageData().get(controller.getSellPageNumbers().get(player)[0])[index], player);
                }
            }
            if(slot == controller.getNextBuyPagePtr()) {
                controller.nextBuyPage(clickedInventory, player);
            }
            if(slot == controller.getPrevBuyPagePtr()) {
                controller.prevBuyPage(clickedInventory, player);
            }
            if(slot == controller.getNextSellPagePtr()) {
                controller.nextSellPage(clickedInventory, player);
            }
            if(slot == controller.getPrevSellPagePtr()) {
                controller.prevSellPage(clickedInventory, player);
            }
            if(slot == controller.getCreateBuyOrder()) {
                shopMenu.getBuyOrderMenu().getController().openBuyOrderMenu(clickedInventory, controller.getItem(), player);
            }
            if(slot == controller.getCreateSellOrder()) {
                shopMenu.getSellOrderMenu().getController().openSellOrderMenu(clickedInventory, controller.getItem(), player);
            }
            if(slot == controller.getPrev()) {
                shopMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getItem().getShopSection(), player);
            }
            if(slot == controller.getRefresh()) {
                controller.reload(clickedInventory, player);
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
