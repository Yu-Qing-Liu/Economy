package com.github.yuqingliu.economy.view.shopmenu.quickbuymenu;

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
public class QuickBuyMenu implements Listener {
    private final ShopMenu shopMenu;
    private final QuickBuyMenuController controller;

    public QuickBuyMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new QuickBuyMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.QuickBuyMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getBuyOptions().contains(slot)) {
                int amount = controller.getQuantities()[slot - controller.getBuy1()];
                controller.quickBuy(amount);
            }
            if(slot == controller.getPrev()) {
                shopMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getItem().getShopSection(), player);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }
}
