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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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

        if(vendorMenu.getCurrentMenu() == MenuType.TradeMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(controller.getBuyOptions().contains(slot)) {
                int amount = controller.getQuantities()[slot - controller.getBuy1()];
                double cost = controller.getCurrencyOption().getBuyPrice(amount);
                if(vendorMenu.getCurrencyService().withdrawPlayerPurse(player, controller.getCurrencyOption().getCurrencyName(), cost)) {
                    vendorMenu.addItemToPlayer(player, controller.getItem().getIcon().clone(), amount);                            
                } else {
                    player.sendMessage(Component.text("Failed to purchase Item(s).", NamedTextColor.RED));
                }
            }
            if(controller.getSellOptions().contains(slot)) {
                int amount = controller.getQuantities()[slot - controller.getSell1()];
                double profit = controller.getCurrencyOption().getSellPrice(amount);
                if(vendorMenu.removeItemToPlayer(player, controller.getItem().getIcon().clone(), amount)) {
                    if(!vendorMenu.getCurrencyService().depositPlayerPurse(player, controller.getCurrencyOption().getCurrencyName(), profit)) {
                        player.sendMessage(Component.text("Item(s) were not sold.", NamedTextColor.RED));
                        vendorMenu.addItemToPlayer(player, controller.getItem().getIcon().clone(), amount);
                    } 
                }
            }
            if(slot == controller.getPrev()) {
                vendorMenu.getItemMenu().getController().openItemMenu(clickedInventory, controller.getItem().getVendorSection());
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }
}
