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
import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.mainmenu.MainMenuController;
import com.github.yuqingliu.economy.view.shopmenu.sellorderdetails.SellOrderDetailsMenuController;

import lombok.Getter;

@Getter
public class SellOrdersMenu implements Listener {
    private final ShopMenu shopMenu;
    private final PlayerInventoryControllerFactory<SellOrdersMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public SellOrdersMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        shopMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        SellOrdersMenuController controller = controllers.getPlayerInventoryController(player, new SellOrdersMenuController(player, clickedInventory, shopMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(shopMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.SellOrdersMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getSellOrders())) {
                ShopOrderEntity order = controller.getPageData().get(slot);
                shopMenu.getSellOrderDetailsMenu().getControllers().getPlayerInventoryController(player, new SellOrderDetailsMenuController(player, clickedInventory, shopMenu)).openMenu(order);
                return;
            }
            if(Arrays.equals(slot, controller.getNextSellOrdersButton())) {
                controller.nextPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevSellOrdersButton())) {
                controller.prevPage();
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
