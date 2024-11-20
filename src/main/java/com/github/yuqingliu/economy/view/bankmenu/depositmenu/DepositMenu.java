package com.github.yuqingliu.economy.view.bankmenu.depositmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;
import com.github.yuqingliu.economy.view.bankmenu.accountmenu.AccountMenuController;

import lombok.Getter;

@Getter
public class DepositMenu implements Listener {
    private final BankMenu bankMenu;
    private final PlayerInventoryControllerFactory<DepositMenuController> controllers = new PlayerInventoryControllerFactory<>();
    
    public DepositMenu(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
        bankMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        DepositMenuController controller = controllers.getPlayerInventoryController(player, new DepositMenuController(player, clickedInventory, bankMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(bankMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(bankMenu.getPlayerMenuTypes().get(player) == MenuType.DepositMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getCurrencies())) {
                CurrencyEntity currency = controller.getPageData().get(controller.getPageNumber(), slot);
                controller.deposit(currency);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                bankMenu.getAccountMenu().getControllers().getPlayerInventoryController(player, new AccountMenuController(player, clickedInventory, bankMenu)).openMenu(controller.getAccount());
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
            if(Arrays.equals(slot, controller.getExitMenuButton())) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(bankMenu.getDisplayName())) {
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
