package com.github.yuqingliu.economy.view.vendormenu.trademenu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;

import lombok.Getter;

@Getter
public class TradeMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final TradeMenuController controller;

    public TradeMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.controller = new TradeMenuController(vendorMenu);
        vendorMenu.getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(vendorMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(vendorMenu.getPlayerMenuTypes().get(player) == MenuType.TradeMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getBuyOptions().contains(slot)) {
                int amount = controller.getQuantities()[slot - controller.getBuy1()];
                controller.buy(player, amount);
            }
            if(controller.getSellOptions().contains(slot)) {
                int amount = controller.getQuantities()[slot - controller.getSell1()];
                controller.sell(player, amount);
            }
            if(slot == controller.getPrev()) {
                vendorMenu.getTransactionMenu().getController().openTransactionMenu(clickedInventory, controller.getItem(), player);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }
}
