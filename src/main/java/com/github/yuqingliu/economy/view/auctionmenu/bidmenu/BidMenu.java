package com.github.yuqingliu.economy.view.auctionmenu.bidmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu.MenuType;
import com.github.yuqingliu.economy.view.auctionmenu.mainmenu.MainMenuController;

import lombok.Getter;

@Getter
public class BidMenu implements Listener {
    private final AuctionMenu auctionMenu;
    private final PlayerInventoryControllerFactory<BidMenuController> controllers = new PlayerInventoryControllerFactory<>();
    
    public BidMenu(AuctionMenu auctionMenu) {
        this.auctionMenu = auctionMenu;
        auctionMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(auctionMenu.getDisplayName())) {
            return;
        }

        BidMenuController controller = controllers.getPlayerInventoryController(player, new BidMenuController(player, inventory, auctionMenu));
        event.setCancelled(true);

        if(auctionMenu.getPlayerMenuTypes().get(player) == MenuType.BidMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
                inventory.close();
                return;
            }
            if(Arrays.equals(slot, controller.getPreviousMenuButton())) {
                auctionMenu.getMainMenu().getControllers().getPlayerInventoryController(player, new MainMenuController(player, inventory, auctionMenu)).openMenu();
                return;
            }
            if(Arrays.equals(slot, controller.getRefreshButton())) {
                controller.reload();
                return;
            }
            if(Arrays.equals(slot, controller.getChangeBidAmount())) {
                controller.changeBidAmount();
                return;
            }
            if(Arrays.equals(slot, controller.getConfirmBid())) {
                controller.confirmBid();
                return;
            }
            if(Arrays.equals(slot, controller.getCollectButton())) {
                controller.collectAuction();
                return;
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(auctionMenu.getDisplayName())) {
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
