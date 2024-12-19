package com.github.yuqingliu.economy.view.shopmenu.quicksellmenu;

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
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderMenuController;

import lombok.Getter;

@Getter
public class QuickSellMenu implements Listener {
    private final ShopMenu shopMenu;
    private PlayerInventoryControllerFactory<QuickSellMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public QuickSellMenu(ShopMenu shopMenu) {
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

        QuickSellMenuController controller = controllers.getPlayerInventoryController(player, new QuickSellMenuController(player, inventory, shopMenu));
        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.QuickSellMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getSellOptions())) {
                int index = controller.rectangleIndex(slot, controller.getSellOptions());
                int amount = controller.getQuantities()[index];
                controller.quickSell(amount);
                return;
            }
            if(Arrays.equals(slot, controller.getSellInventoryButton())) {
                int amount = shopMenu.getPluginManager().getInventoryManager().countAvailableInventorySpace(player, controller.getItem().getIcon().getType());
                controller.quickSell(amount);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                controller.onClose();
                shopMenu.getOrderMenu().getControllers().getPlayerInventoryController(player, new OrderMenuController(player, clickedInventory, shopMenu)).openMenu(controller.getItem());
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
            Player player = (Player) event.getPlayer();
            Inventory inventory = player.getOpenInventory().getTopInventory();
            controllers.getPlayerInventoryController(player, new QuickSellMenuController(player, inventory, shopMenu)).onClose();
            controllers.removePlayerInventoryController(player);
        }
    }
}
