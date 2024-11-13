package com.github.yuqingliu.economy.view.bankmenu.withdrawmenu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;

import lombok.Getter;

@Getter
public class WithdrawMenu implements Listener {
    private final BankMenu bankMenu;
    private final WithdrawMenuController controller;
    
    public WithdrawMenu(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
        this.controller = new WithdrawMenuController(bankMenu);
        bankMenu.getPluginManager().getEventManager().registerEvent(this);
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

        if(bankMenu.getPlayerMenuTypes().get(player) == MenuType.WithdrawMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = bankMenu.toCoords(event.getSlot());
            if(bankMenu.isUnavailable(currentItem)) {
                return;
            }
            if(bankMenu.rectangleContains(slot, controller.getCurrencies())) {
                int pageNumber = controller.getPageNumbers().get(player)[0];
                CurrencyEntity currency = controller.getPageData().get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                controller.withdraw(clickedInventory, player, currency);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevMenuButton())) {
                bankMenu.getAccountMenu().getController().openAccountMenu(player, clickedInventory, controller.getAccounts().get(player));
                return;
            }
            if(Arrays.equals(slot, controller.getNextPage())) {
                controller.nextPage(player, clickedInventory);
                return;
            }
            if(Arrays.equals(slot, controller.getPrevPage())) {
                controller.prevPage(player, clickedInventory);
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
            controller.onClose((Player) event.getPlayer());
        }
    }
}
