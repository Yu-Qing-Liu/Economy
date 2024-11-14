package com.github.yuqingliu.economy.view.shopmenu.sellordersmenu;

import java.util.Arrays;

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
public class SellOrdersMenu implements Listener {
    private final ShopMenu shopMenu;
    private final SellOrdersMenuController controller;

    public SellOrdersMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new SellOrdersMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.SellOrdersMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = shopMenu.toCoords(event.getSlot());
            if(shopMenu.isUnavailable(currentItem)) {
                return;
            }
            if(shopMenu.rectangleContains(slot, controller.getSellOrders())) {
                int pageNumber = controller.getPageNumbers().get(player)[0];
                ShopOrderEntity order = controller.getPageData().get(player).get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                shopMenu.getSellOrderDetailsMenu().getController().openSellOrderDetailsMenu(clickedInventory, order, player);
                return;
            }
            if(Arrays.equals(slot, controller.getNextSellOrdersButton())) {
                controller.nextSellOrdersPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevSellOrdersButton())) {
                controller.prevSellOrdersPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getMainMenu().getController().openMainMenu(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getReloadButton())) {
                controller.reload(clickedInventory, player);
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
