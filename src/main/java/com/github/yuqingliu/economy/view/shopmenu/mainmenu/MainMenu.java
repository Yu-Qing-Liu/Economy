package com.github.yuqingliu.economy.view.shopmenu.mainmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final ShopMenu shopMenu;
    private final MainMenuController controller;

    public MainMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.controller = new MainMenuController(shopMenu);
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

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = shopMenu.toCoords(event.getSlot());
            if(shopMenu.isUnavailable(currentItem)) {
                return;
            }
            if(shopMenu.rectangleContains(slot, controller.getSectionsOptions())) {
                controller.displayInitialItems(clickedInventory, player, slot);
                return;
            }
            if(shopMenu.rectangleContains(slot, controller.getItemsOptions())) {
                int pageNumber = controller.getItemPageNumbers().get(player)[0];
                ShopItemEntity item = controller.getPageItemData().get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                shopMenu.getOrderMenu().getController().openOrderMenu(clickedInventory, item, player);
                return;
            }
            if(Arrays.equals(slot, controller.getBuyOrdersMenuButton())) {
                shopMenu.getBuyOrdersMenu().getController().openBuyOrdersMenu(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getSellOrdersMenuButton())) {
                shopMenu.getSellOrdersMenu().getController().openSellOrdersMenu(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getNextSectionsButton())) {
                controller.nextSectionPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevSectionsButton())) {
                controller.prevSectionPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getNextItemsButton())) {
                controller.nextItemPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevItemsButton())) {
                controller.prevItemPage(clickedInventory, player);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
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
