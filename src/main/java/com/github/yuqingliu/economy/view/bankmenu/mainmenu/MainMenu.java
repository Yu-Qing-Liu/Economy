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
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;

import lombok.Getter;

@Getter
public class MainMenu implements Listener {
    private final BankMenu bankMenu;
    private final MainMenuController controller;
    
    public MainMenu(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
        this.controller = new MainMenuController(bankMenu);
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

        if(bankMenu.getPlayerMenuTypes().get(player) == MenuType.MainMenu && clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
            int[] slot = bankMenu.toCoords(event.getSlot());
            if(bankMenu.isUnavailable(currentItem)) {
                return;
            }
            if(bankMenu.rectangleContains(slot, controller.getAccounts())) {
                int pageNumber = controller.getPageNumbers().get(player)[0];
                AccountEntity account = controller.getPageData().get(pageNumber).get(Arrays.asList(slot[0], slot[1]));
                bankMenu.getAccountMenu().getController().openAccountMenu(player, clickedInventory, account);
                return;
            }
            if(Arrays.equals(slot, controller.getNextPageButton())) {
                controller.nextPage(player, clickedInventory);
                return;
            } 
            if(Arrays.equals(slot, controller.getPrevPageButton())) {
                controller.prevPage(player, clickedInventory);
                return;
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
