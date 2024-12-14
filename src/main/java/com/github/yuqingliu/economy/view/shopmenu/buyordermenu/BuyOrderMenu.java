package com.github.yuqingliu.economy.view.shopmenu.buyordermenu;

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

import lombok.Getter;

@Getter
public class BuyOrderMenu implements Listener {
    private final ShopMenu shopMenu;
    private final PlayerInventoryControllerFactory<BuyOrderMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public BuyOrderMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        shopMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        BuyOrderMenuController controller = controllers.getPlayerInventoryController(player, new BuyOrderMenuController(player, clickedInventory, shopMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(shopMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.BuyOrderMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                controller.onClose();
                shopMenu.getOrderMenu().getController().openMenu(clickedInventory, controller.getItem(), player);
                return;
            }
            if(Arrays.equals(slot, controller.getSetCurrencyTypeButton())) {
                controller.setCurrencyType();
                return;
            }
            if(Arrays.equals(slot, controller.getSetQuantityButton())) {
                controller.setQuantity();
                return;
            }
            if(Arrays.equals(slot, controller.getSetPriceButton())) {
                controller.setUnitPrice();
                return;
            }
            if(Arrays.equals(slot, controller.getConfirmOrderButton())) {
                controller.confirmOrder();
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
            controllers.getPlayerInventoryController((Player) event.getPlayer(), null).onClose();
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
