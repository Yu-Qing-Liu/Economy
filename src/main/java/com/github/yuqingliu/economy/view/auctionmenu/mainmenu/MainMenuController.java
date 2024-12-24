package com.github.yuqingliu.economy.view.auctionmenu.mainmenu;

import java.time.Duration;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu.MenuType;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;

import lombok.Getter;

@Getter
public class MainMenuController extends AbstractPlayerInventoryController<AuctionMenu> {
    private final int[] nextPageButton = new int[]{8,1};
    private final int[] prevPageButton = new int[]{8,2};
    private final int[] refreshButton = new int[]{8,3};
    private final int[] exitMenuButton = new int[]{8,4};

    public MainMenuController(Player player, Inventory inventory, AuctionMenu auctionMenu) {
        super(player, inventory, auctionMenu);
    }
    
    public void openMenu() {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.YELLOW_STAINED_GLASS_PANE));
        buttons();
        Scheduler.runAsync((task) -> {
            
        });
    }

    private void buttons() {
        setItem(nextPageButton, getNextPageIcon());
        setItem(prevPageButton, getPrevPageIcon());
        setItem(refreshButton, getReloadIcon());
        setItem(exitMenuButton, getExitMenuIcon());
    }
}
