package com.github.yuqingliu.economy.view.shopmenu.sellordermenu;

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
public class SellOrderMenu implements Listener {
    private final ShopMenu shopMenu;
    private final PlayerInventoryControllerFactory<SellOrderMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public SellOrderMenu(ShopMenu shopMenu) {
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

        SellOrderMenuController controller = controllers.getPlayerInventoryController(player, new SellOrderMenuController(player, inventory, shopMenu));
        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.SellOrderMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getOrderMenu().getControllers().getPlayerInventoryController(player, new OrderMenuController(player, clickedInventory, shopMenu)).openMenu(controller.getItem());
                return;
            }
            if(Arrays.equals(slot, controller.getChangeCurrencyTypeButton())) {
                controller.changeCurrencyType();
                return;
            }
            if(Arrays.equals(slot, controller.getChangeQuantityButton())) {
                controller.changeQuantity();
                return;
            }
            if(Arrays.equals(slot, controller.getChangePriceButton())) {
                controller.changeUnitPrice();
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
        Player player = (Player) event.getPlayer();
        if (event.getView().title().equals(shopMenu.getDisplayName())) {
            controllers.removePlayerInventoryController(player);
        }
    }
}
