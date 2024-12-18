package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.buyordermenu.BuyOrderMenuController;
import com.github.yuqingliu.economy.view.shopmenu.mainmenu.MainMenuController;
import com.github.yuqingliu.economy.view.shopmenu.quickbuymenu.QuickBuyMenuController;
import com.github.yuqingliu.economy.view.shopmenu.quicksellmenu.QuickSellMenuController;
import com.github.yuqingliu.economy.view.shopmenu.sellordermenu.SellOrderMenuController;

import lombok.Getter;

@Getter
public class OrderMenu implements Listener {
    private final ShopMenu shopMenu;
    private PlayerInventoryControllerFactory<OrderMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public OrderMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        shopMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(shopMenu.getDisplayName())) {
            return;
        }

        OrderMenuController controller = controllers.getPlayerInventoryController(player, new OrderMenuController(player, inventory, shopMenu));
        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.OrderMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getBuyOrders())) {
                OrderOption orderOption = controller.getBuyPageData().get(slot);
                shopMenu.getQuickSellMenu().getControllers().getPlayerInventoryController(player, new QuickSellMenuController(player, clickedInventory, shopMenu)).openMenu(controller.getItem(), orderOption);
                return;
            }
            if(controller.rectangleContains(slot, controller.getSellOrders())) {
                OrderOption orderOption = controller.getSellPageData().get(slot);
                shopMenu.getQuickBuyMenu().getControllers().getPlayerInventoryController(player, new QuickBuyMenuController(player, clickedInventory, shopMenu)).openMenu(controller.getItem(), orderOption);
                return;
            }
            if(Arrays.equals(slot, controller.getCreateBuyOrderButton())) {
                shopMenu.getBuyOrderMenu().getControllers().getPlayerInventoryController(player, new BuyOrderMenuController(player, clickedInventory, shopMenu)).openMenu(controller.getItem());
                return;
            }
            if(Arrays.equals(slot, controller.getCreateSellOrderButton())) {
                shopMenu.getSellOrderMenu().getControllers().getPlayerInventoryController(player, new SellOrderMenuController(player, clickedInventory, shopMenu)).openMenu(controller.getItem());
                return;
            }
            if(Arrays.equals(slot, controller.getRefreshButton())) {
                controller.reload();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getMainMenu().getControllers().getPlayerInventoryController(player, new MainMenuController(player, clickedInventory, shopMenu)).openMenu();
                return;
            }
            if(Arrays.equals(slot, controller.getNextBuyOrdersButton())) {
                controller.nextBuyPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevBuyOrdersButton())) {
                controller.prevBuyPage();
                return;
            }
            if(Arrays.equals(slot, controller.getNextSellOrdersButton())) {
                controller.nextSellPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevSellOrdersButton())) {
                controller.prevSellPage();
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
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
