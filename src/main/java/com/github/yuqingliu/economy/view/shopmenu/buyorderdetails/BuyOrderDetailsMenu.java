package com.github.yuqingliu.economy.view.shopmenu.buyorderdetails;

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
public class BuyOrderDetailsMenu implements Listener {
    private final ShopMenu shopMenu;
    private final PlayerInventoryControllerFactory<BuyOrderDetailsMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public BuyOrderDetailsMenu(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        shopMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        BuyOrderDetailsMenuController controller = controllers.getPlayerInventoryController(player, new BuyOrderDetailsMenuController(player, clickedInventory, shopMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(shopMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.BuyOrderDetailsMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                shopMenu.getBuyOrdersMenu().getController().openBuyOrdersMenu(clickedInventory, player);
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
        if(event.getView().title().equals(shopMenu.getDisplayName())) {
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
