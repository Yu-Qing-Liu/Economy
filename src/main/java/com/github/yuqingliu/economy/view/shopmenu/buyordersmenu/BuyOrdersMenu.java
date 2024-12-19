package com.github.yuqingliu.economy.view.shopmenu.buyordersmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.buyorderdetails.BuyOrderDetailsMenuController;
import com.github.yuqingliu.economy.view.shopmenu.mainmenu.MainMenuController;

import lombok.Getter;

@Getter
public class BuyOrdersMenu implements Listener {
    private final ShopMenu shopMenu;
    private final PlayerInventoryControllerFactory<BuyOrdersMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public BuyOrdersMenu(ShopMenu shopMenu) {
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

        BuyOrdersMenuController controller = controllers.getPlayerInventoryController(player, new BuyOrdersMenuController(player, inventory, shopMenu));
        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.BuyOrdersMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getBuyOrders())) {
                ShopOrderEntity order = controller.getPageData().get(slot);
                shopMenu.getBuyOrderDetailsMenu().getControllers().getPlayerInventoryController(player, new BuyOrderDetailsMenuController(player, clickedInventory, shopMenu)).openMenu(order);
                return;
            }
            if(Arrays.equals(slot, controller.getNextBuyOrdersButton())) {
                controller.nextBuyOrdersPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevBuyOrdersButton())) {
                controller.prevBuyOrdersPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getMainMenu().getControllers().getPlayerInventoryController(player, new MainMenuController(player, clickedInventory, shopMenu)).openMenu();
                return;
            }
            if(Arrays.equals(slot, controller.getReloadButton())) {
                controller.reload();
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
