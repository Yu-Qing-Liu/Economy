package com.github.yuqingliu.economy.view.auctionmenu.mainmenu;

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
import com.github.yuqingliu.economy.view.auctionmenu.playerauctions.PlayerAuctionsMenuController;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final AuctionMenu auctionMenu;
    private final PlayerInventoryControllerFactory<MainMenuController> controllers = new PlayerInventoryControllerFactory<>();
    
    public MainMenu(AuctionMenu auctionMenu) {
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

        MainMenuController controller = controllers.getPlayerInventoryController(player, new MainMenuController(player, inventory, auctionMenu));
        event.setCancelled(true);

        if(auctionMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getPlayerAuctionsButton())) {
                auctionMenu.getPlayerAuctionsMenu().getControllers().getPlayerInventoryController(player, new PlayerAuctionsMenuController(player, inventory, auctionMenu)).openMenu();
                return;
            }
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
                inventory.close();
                return;
            }
            if(Arrays.equals(slot, controller.getRefreshButton())) {
                controller.reload();
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
