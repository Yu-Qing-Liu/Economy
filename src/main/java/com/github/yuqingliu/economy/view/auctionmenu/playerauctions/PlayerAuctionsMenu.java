package com.github.yuqingliu.economy.view.auctionmenu.playerauctions;

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

import lombok.Getter;

@Getter
public class PlayerAuctionsMenu implements Listener {
    private final AuctionMenu auctionMenu;
    private final PlayerInventoryControllerFactory<PlayerAuctionsMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public PlayerAuctionsMenu(AuctionMenu auctionMenu) {
        this.auctionMenu = auctionMenu;
        auctionMenu.getPluginManager().getEventManager().registerEvent(this);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        ItemStack currentItem = event.getCurrentItem();

        if(clickedInventory == null || currentItem == null || !event.getView().title().equals(auctionMenu.getDisplayName())) {
            return;
        }

        PlayerAuctionsMenuController controller = controllers.getPlayerInventoryController(player, new PlayerAuctionsMenuController(player, inventory, auctionMenu));
        event.setCancelled(true);

        if(auctionMenu.getPlayerMenuTypes().get(player) == MenuType.PlayerAuctionsMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getView().title().equals(auctionMenu.getDisplayName())) {
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
