package com.github.yuqingliu.economy.view.shopmenu.mainmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.buyordersmenu.BuyOrdersMenuController;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderMenuController;
import com.github.yuqingliu.economy.view.shopmenu.sellordersmenu.SellOrdersMenuController;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final ShopMenu shopMenu;
    private final PlayerInventoryControllerFactory<MainMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public MainMenu(ShopMenu shopMenu) {
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

        MainMenuController controller = controllers.getPlayerInventoryController(player, new MainMenuController(player, inventory, shopMenu));
        event.setCancelled(true);

        if(shopMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getSectionsOptions())) {
                controller.displayInitialItems(clickedInventory, player, slot);
                return;
            }
            if(controller.rectangleContains(slot, controller.getItemsOptions())) {
                ShopItemEntity item = controller.getItemPageData().get(slot);
                shopMenu.getOrderMenu().getControllers().getPlayerInventoryController(player, new OrderMenuController(player, clickedInventory, shopMenu)).openMenu(item);
                return;
            }
            if(Arrays.equals(slot, controller.getBuyOrdersMenuButton())) {
                shopMenu.getBuyOrdersMenu().getControllers().getPlayerInventoryController(player, new BuyOrdersMenuController(player, clickedInventory, shopMenu)).openMenu();
                return;
            }
            if(Arrays.equals(slot, controller.getSellOrdersMenuButton())) {
                shopMenu.getSellOrdersMenu().getControllers().getPlayerInventoryController(player, new SellOrdersMenuController(player, clickedInventory, shopMenu)).openMenu();
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
