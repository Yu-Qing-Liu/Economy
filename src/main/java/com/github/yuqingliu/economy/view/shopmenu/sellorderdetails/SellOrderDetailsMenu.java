package com.github.yuqingliu.economy.view.shopmenu.sellorderdetails;

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
import com.github.yuqingliu.economy.view.shopmenu.sellordersmenu.SellOrdersMenuController;

import lombok.Getter;

@Getter
public class SellOrderDetailsMenu implements Listener {
    private final ShopMenu shopMenu;
    private final PlayerInventoryControllerFactory<SellOrderDetailsMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public SellOrderDetailsMenu(ShopMenu shopMenu) {
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

        SellOrderDetailsMenuController controller = controllers.getPlayerInventoryController(player, new SellOrderDetailsMenuController(player, inventory, shopMenu));
        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.SellOrderDetailsMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getSellOrdersMenu().getControllers().getPlayerInventoryController(player, new SellOrdersMenuController(player, clickedInventory, shopMenu)).openMenu();
                return;
            }
            if(Arrays.equals(slot, controller.getCancelOrderButton())) {
                controller.cancelOrder();
                return;
            }
            if(Arrays.equals(slot, controller.getClaimOrderButton())) {
                controller.claimOrder();
                return;
            }
            if(Arrays.equals(slot, controller.getRefreshButton())) {
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
