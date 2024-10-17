package com.github.yuqingliu.economy.view.shopmenu.quicksellmenu;

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
public class QuickSellMenu implements Listener {
    private final ShopMenu shopMenu;
    private final QuickSellMenuController controller;

    public QuickSellMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new QuickSellMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.QuickSellMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getSellOptions().contains(slot)) {
                int amount = controller.getQuantities()[slot - controller.getSell1()];
                controller.quickSell(amount, player);
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