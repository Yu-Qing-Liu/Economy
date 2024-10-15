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

        if(shopMenu.getCurrentMenu() == MenuType.OrderMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getBuyOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getBuyOptions().get(0);
                if(controller.getBuyPageData() != null && controller.getBuyPageData().containsKey(controller.getBuyPageNumber())) {
                    // shopMenu.getQuickBuyMenu().getController().openTradeMenu(clickedInventory, controller.getItem(), controller.getBuyPageData().get(controller.getBuyPageNumber())[index]);
                }
            }
            if(slot == controller.getNextBuyPagePtr()) {
                controller.nextBuyPage(clickedInventory);
            }
            if(slot == controller.getPrevBuyPagePtr()) {
                controller.prevBuyPage(clickedInventory);
            }
            if(slot == controller.getNextSellPagePtr()) {
                controller.nextSellPage(clickedInventory);
            }
            if(slot == controller.getPrevSellPagePtr()) {
                controller.prevSellPage(clickedInventory);
            }
            if(slot == controller.getCreateBuyOrder()) {
                shopMenu.getBuyOrderMenu().getController().openBuyOrderMenu(clickedInventory, controller.getItem(), player);
            }
            if(slot == controller.getCreateSellOrder()) {

            }
            if(slot == controller.getPrev()) {
                shopMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getItem().getShopSection());
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
