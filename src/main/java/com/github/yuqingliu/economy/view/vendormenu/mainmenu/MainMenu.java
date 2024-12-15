package com.github.yuqingliu.economy.view.vendormenu.mainmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;
import com.github.yuqingliu.economy.view.vendormenu.transactionmenu.TransactionMenuController;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final PlayerInventoryControllerFactory<MainMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public MainMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        vendorMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        MainMenuController controller = controllers.getPlayerInventoryController(player, new MainMenuController(player, clickedInventory, vendorMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(vendorMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(vendorMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getSectionsOptions())) {
                controller.displayInitialItems(slot);
                return;
            }
            if(controller.rectangleContains(slot, controller.getItemsOptions())) {
                VendorItemEntity item = controller.getItemPageData().get(slot);
                vendorMenu.getTransactionMenu().getControllers().getPlayerInventoryController(player, new TransactionMenuController(player, clickedInventory, vendorMenu)).openMenu(item);;
                return;
            }
            if(Arrays.equals(slot, controller.getNextSectionsButton())) {
                controller.nextSectionPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevSectionsButton())) {
                controller.prevSectionPage();
                return;
            }
            if(Arrays.equals(slot, controller.getNextItemsButton())) {
                controller.nextItemPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevItemsButton())) {
                controller.prevItemPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
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
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
