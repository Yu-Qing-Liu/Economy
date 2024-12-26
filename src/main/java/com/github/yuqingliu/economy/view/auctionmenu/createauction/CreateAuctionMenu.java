package com.github.yuqingliu.economy.view.auctionmenu.createauction;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.view.PlayerInventoryControllerFactory;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu.MenuType;
import com.github.yuqingliu.economy.view.auctionmenu.mainmenu.MainMenuController;

import lombok.Getter;

@Getter
public class CreateAuctionMenu implements Listener {
    private final AuctionMenu auctionMenu;
    private final PlayerInventoryControllerFactory<CreateAuctionMenuController> controllers = new PlayerInventoryControllerFactory<>();

    public CreateAuctionMenu(AuctionMenu auctionMenu) {
        this.auctionMenu = auctionMenu;
        auctionMenu.getPluginManager().getEventManager().registerEvent(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        Inventory playerInventory = player.getOpenInventory().getBottomInventory();
        ItemStack currentItem = event.getCurrentItem();

        if (clickedInventory == null || currentItem == null
                || !event.getView().title().equals(auctionMenu.getDisplayName())) {
            return;
        }

        CreateAuctionMenuController controller = controllers.getPlayerInventoryController(player,
                new CreateAuctionMenuController(player, inventory, auctionMenu));
        event.setCancelled(true);

        if (auctionMenu.getPlayerMenuTypes().get(player) == MenuType.CreateAuctionMenu
                && clickedInventory.equals(playerInventory)) {
            ItemStack selectedItem = clickedInventory.getItem(event.getSlot());
            if (selectedItem == null || selectedItem.getType() == Material.AIR) {
                return;
            }
            ItemStack slotItem = controller.getItem(controller.getItemSlot());
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                controller.setItem(controller.getItemSlot(), selectedItem);
                clickedInventory.setItem(event.getSlot(), new ItemStack(Material.AIR));
            } else {
                auctionMenu.getPluginManager().getInventoryManager().addItemToPlayer(player, slotItem, slotItem.getAmount());
                controller.setItem(controller.getItemSlot(), selectedItem);
                clickedInventory.setItem(event.getSlot(), new ItemStack(Material.AIR));
            }
        }

        if (auctionMenu.getPlayerMenuTypes().get(player) == MenuType.CreateAuctionMenu
                && clickedInventory.equals(inventory)) {
            int[] slot = controller.toCoords(event.getSlot());
            if (controller.isUnavailable(currentItem)) {
                return;
            }
            if (Arrays.equals(slot, controller.getItemSlot())) {
                controller.onClose();
                return;
            }
            if (Arrays.equals(slot, controller.getExitMenuButton())) {
                inventory.close();
                return;
            }
            if (Arrays.equals(slot, controller.getPreviousMenuButton())) {
                auctionMenu.getMainMenu().getControllers()
                        .getPlayerInventoryController(player, new MainMenuController(player, inventory, auctionMenu))
                        .openMenu();
                return;
            }
            if (Arrays.equals(slot, controller.getChangeStartingBid())) {
                controller.changeStartingBid();
                return;
            }
            if (Arrays.equals(slot, controller.getChangeBidCurrency())) {
                controller.changeBidCurrency();
                return;
            }
            if (Arrays.equals(slot, controller.getChangeAuctionDelay())) {
                controller.changeAuctionDelay();
                return;
            }
            if (Arrays.equals(slot, controller.getChangeAuctionDuration())) {
                controller.changeAuctionDuration();
                return;
            }
            if (Arrays.equals(slot, controller.getPlaceItemIndicator())) {
                return;
            }
            if (Arrays.equals(slot, controller.getConfirmButton())) {
                controller.confirm();
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getView().title().equals(auctionMenu.getDisplayName()) && auctionMenu.getPlayerMenuTypes().get(player) == MenuType.CreateAuctionMenu) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            controllers.getPlayerInventoryController(player, new CreateAuctionMenuController(player, inventory, auctionMenu)).onClose();
            controllers.removePlayerInventoryController(player);
        }
    }
}
