package com.github.yuqingliu.economy.view.bankmenu.mainmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;
import com.github.yuqingliu.economy.view.bankmenu.accountmenu.AccountMenuController;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final BankMenu bankMenu;
    private final PlayerInventoryControllerFactory<MainMenuController> controllers = new PlayerInventoryControllerFactory<>();
    
    public MainMenu(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
        bankMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        MainMenuController controller = controllers.getPlayerInventoryController(player, new MainMenuController(player, clickedInventory, bankMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(bankMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(bankMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(controller.rectangleContains(slot, controller.getAccounts())) {
                AccountEntity account = controller.getPageData().get(controller.getPageNumber(), slot);
                if(controller.unlockAccount(account)) {
                    bankMenu.getAccountMenu().getControllers().getPlayerInventoryController(player, new AccountMenuController(player, clickedInventory, bankMenu)).openMenu(account);
                }
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
        if (event.getView().title().equals(bankMenu.getDisplayName())) {
            controllers.removePlayerInventoryController((Player) event.getPlayer());
        }
    }
}
