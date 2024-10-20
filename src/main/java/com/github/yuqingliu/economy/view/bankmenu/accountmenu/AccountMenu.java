package com.github.yuqingliu.economy.view.bankmenu.accountmenu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;

import lombok.Getter;

@Getter
public class AccountMenu implements Listener {
    private final BankMenu bankMenu;
    private final int prevPagePtr = 10;
    private final int nextPagePtr = 16;
    private final AccountMenuController controller;
    
    public AccountMenu(BankMenu bankMenu) {
        this.bankMenu = bankMenu;

        this.controller = new AccountMenuController(bankMenu);
        bankMenu.getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null || !event.getView().title().equals(bankMenu.getDisplayName())) {
            return;
        }

        event.setCancelled(true);

        if(bankMenu.getPlayerMenuTypes().get(player) == MenuType.AccountMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int slot = event.getSlot();
            if(slot == controller.getPrev()) {
                bankMenu.getMainMenu().getController().openMainMenu(player, clickedInventory);
            }
            if(slot == controller.getExit()) {
                clickedInventory.close();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(bankMenu.getDisplayName())) {
            controller.onClose((Player) event.getPlayer());
        }
    }
}
