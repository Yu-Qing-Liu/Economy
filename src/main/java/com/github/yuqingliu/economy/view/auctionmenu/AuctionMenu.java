package com.github.yuqingliu.economy.view.auctionmenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.persistence.services.AuctionService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.auctionmenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.auctionmenu.mainmenu.MainMenuController;
import com.github.yuqingliu.economy.view.auctionmenu.playerauctions.PlayerAuctionsMenu;
import com.google.inject.Inject;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class AuctionMenu extends AbstractPlayerInventory {
    private final AuctionService auctionService;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu, PlayerAuctionsMenu;
    }

    private final MainMenu mainMenu;
    private final PlayerAuctionsMenu playerAuctionsMenu;

    @Inject
    public AuctionMenu(PluginManager pluginManager, Logger logger, Component displayName, AuctionService auctionService) {
        super(pluginManager, logger, displayName, 54);
        this.auctionService = auctionService;
        this.mainMenu = new MainMenu(this);
        this.playerAuctionsMenu = new PlayerAuctionsMenu(this);
    }

    @Override
    public Inventory load(Player player) {
        Inventory inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        return inventory;
    }

    @Override
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        mainMenu.getControllers().getPlayerInventoryController(player, new MainMenuController(player, inventory, this)).openMenu();
    }
}
