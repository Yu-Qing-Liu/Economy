package com.github.yuqingliu.economy.view.pursemenu.mainmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu.MenuType;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final PurseMenu purseMenu;
    private final PlayerInventoryControllerFactory<MainMenuController> controllers = new PlayerInventoryControllerFactory<>();
    
    public MainMenu(PurseMenu purseMenu) {
        this.purseMenu = purseMenu;
        purseMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(purseMenu.getDisplayName())) {
            return;
        }

        MainMenuController controller = controllers.getPlayerInventoryController(player, new MainMenuController(player, inventory, purseMenu));
        event.setCancelled(true);

        if(purseMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getNextPageButton())) {
                controller.nextPage();
                return;
            } 
            if(Arrays.equals(slot, controller.getPrevPageButton())) {
                controller.prevPage();
                return;
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(purseMenu.getDisplayName())) {
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
