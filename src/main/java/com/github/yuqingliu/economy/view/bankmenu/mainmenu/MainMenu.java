package com.github.yuqingliu.economy.view.bankmenu.mainmenu;

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
            int slot = event.getSlot();
            if(controller.getOptions().contains(slot) && currentItem.getType() != controller.getVoidOption()) {
                int index = slot - controller.getOptions().get(0);
                AccountEntity account = controller.getPageData().get(controller.getPageNumbers().get(player)[0])[index];
                if(controller.unlockAccount(account, player)) {
                    if(controller.getPageData() != null && controller.getPageData().containsKey(controller.getPageNumbers().get(player)[0])) {
                        bankMenu.getAccountMenu().getController().openAccountMenu(player, clickedInventory, account);
                    }
                }
            }
            if(slot == controller.getNextPagePtr()) {
                controller.nextPage(player, clickedInventory);
            } 
            if(slot == controller.getPrevPagePtr()) {
                controller.prevPage(player, clickedInventory);
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
