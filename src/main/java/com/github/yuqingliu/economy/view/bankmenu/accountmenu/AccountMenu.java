package com.github.yuqingliu.economy.view.bankmenu.accountmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;
import com.github.yuqingliu.economy.view.bankmenu.depositmenu.DepositMenuController;
import com.github.yuqingliu.economy.view.bankmenu.mainmenu.MainMenuController;
import com.github.yuqingliu.economy.view.bankmenu.withdrawmenu.WithdrawMenuController;

import lombok.Getter;

@Getter
public class AccountMenu implements Listener {
    private final BankMenu bankMenu;
    private final PlayerInventoryControllerFactory<AccountMenuController> controllers = new PlayerInventoryControllerFactory<>();
    
    public AccountMenu(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
        bankMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        AccountMenuController controller = controllers.getPlayerInventoryController(player, new AccountMenuController(player, clickedInventory, bankMenu));

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(bankMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(bankMenu.getPlayerMenuTypes().get(player) == MenuType.AccountMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = controller.toCoords(event.getSlot());
            if(controller.isUnavailable(currentItem)) {
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                bankMenu.getMainMenu().getControllers().getPlayerInventoryController(player, new MainMenuController(player, clickedInventory, bankMenu)).openMenu();
                return;
            }
            if(Arrays.equals(slot, controller.getDepositButton())) {
                bankMenu.getDepositMenu().getControllers().getPlayerInventoryController(player, new DepositMenuController(player, clickedInventory, bankMenu)).openMenu(controller.getAccount());
                return;
            }
            if(Arrays.equals(slot, controller.getWithdrawButton())) {
                bankMenu.getWithdrawMenu().getControllers().getPlayerInventoryController(player, new WithdrawMenuController(player, clickedInventory, bankMenu)).openMenu(controller.getAccount());
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
