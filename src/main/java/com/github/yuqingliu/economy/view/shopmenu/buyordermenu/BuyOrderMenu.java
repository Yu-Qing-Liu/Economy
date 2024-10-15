package com.github.yuqingliu.economy.view.shopmenu.buyordermenu;

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
public class BuyOrderMenu implements Listener {
    private final ShopMenu shopMenu;
    private final BuyOrderMenuController controller;

    public BuyOrderMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new BuyOrderMenuController(shopMenu);
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

        if(shopMenu.getCurrentMenu() == MenuType.BuyOrderMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(slot == controller.getPrev()) {
                shopMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getItem().getShopSection());
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
            if(slot == controller.getSetCurrencyType()) {
                controller.setCurrencyType(clickedInventory);
            }
        }
    }
}
