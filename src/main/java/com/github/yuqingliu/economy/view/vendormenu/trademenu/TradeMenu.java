package com.github.yuqingliu.economy.view.vendormenu.trademenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;
import com.github.yuqingliu.economy.view.vendormenu.transactionmenu.TransactionMenuController;

import lombok.Getter;

@Getter
public class TradeMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final PlayerInventoryControllerFactory<TradeMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public TradeMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        vendorMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        TradeMenuController controller = controllers.getPlayerInventoryController(player, new TradeMenuController(player, clickedInventory, vendorMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(vendorMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(vendorMenu.getPlayerMenuTypes().get(player) == MenuType.TradeMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getBuyOptions())) {
                int index = controller.rectangleIndex(slot, controller.getBuyOptions());
                controller.buy(controller.getQuantities()[index]);
                return;
            }
            if(controller.rectangleContains(slot, controller.getSellOptions())) {
                int index = controller.rectangleIndex(slot, controller.getSellOptions());
                controller.sell(controller.getQuantities()[index]);
                return;
            }
            if(Arrays.equals(slot, controller.getBuyInventoryButton())) {
                int amount = vendorMenu.getPluginManager().getInventoryManager().countAvailableInventorySpace(player, controller.getItem().getIcon().getType());
                controller.buy(amount);
                return;
            }
            if(Arrays.equals(slot, controller.getSellInventoryButton())) {
                int amount = vendorMenu.getPluginManager().getInventoryManager().countItemFromPlayer(player, controller.getItem().getIcon());
                controller.sell(amount);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                controller.onClose();
                vendorMenu.getTransactionMenu().getControllers().getPlayerInventoryController(player, new TransactionMenuController(player, clickedInventory, vendorMenu)).openMenu(controller.getItem());
                return;
            }
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(vendorMenu.getDisplayName())) {
            controllers.getPlayerInventoryController((Player) event.getPlayer(), null).onClose();
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
