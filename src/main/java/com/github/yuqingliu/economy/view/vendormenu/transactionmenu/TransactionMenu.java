package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;
import com.github.yuqingliu.economy.view.vendormenu.mainmenu.MainMenuController;
import com.github.yuqingliu.economy.view.vendormenu.trademenu.TradeMenuController;

import lombok.Getter;

@Getter
public class TransactionMenu implements Listener {
    private final VendorMenu vendorMenu;
    private final PlayerInventoryControllerFactory<TransactionMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public TransactionMenu(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        vendorMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        TransactionMenuController controller = controllers.getPlayerInventoryController(player, new TransactionMenuController(player, clickedInventory, vendorMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(vendorMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(vendorMenu.getPlayerMenuTypes().get(player) == MenuType.TransactionMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getCurrencyOptions())) {
                CurrencyOption currencyOption = controller.getPageData().get(slot);
                vendorMenu.getTradeMenu().getControllers().getPlayerInventoryController(player, new TradeMenuController(player, clickedInventory, vendorMenu)).openMenu(controller.getItem(), currencyOption);
                return;
            }
            if(Arrays.equals(slot, controller.getNextOptionsButton())) {
                controller.nextPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevOptionsButton())) {
                controller.prevPage();
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                vendorMenu.getMainMenu().getControllers().getPlayerInventoryController(player, new MainMenuController(player, clickedInventory, vendorMenu)).openMenu();
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
