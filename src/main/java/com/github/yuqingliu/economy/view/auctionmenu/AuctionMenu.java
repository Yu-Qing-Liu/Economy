package com.github.yuqingliu.economy.view.auctionmenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.persistence.services.AuctionService;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.auctionmenu.bidmenu.BidMenu;
import com.github.yuqingliu.economy.view.auctionmenu.createauction.CreateAuctionMenu;
import com.github.yuqingliu.economy.view.auctionmenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.auctionmenu.mainmenu.MainMenuController;
import com.github.yuqingliu.economy.view.auctionmenu.playerauctions.PlayerAuctionsMenu;
import com.google.inject.Inject;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class AuctionMenu extends AbstractPlayerInventory {
    private final AuctionService auctionService;
    private final CurrencyService currencyService;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu, PlayerAuctionsMenu, BidMenu, CreateAuctionMenu;
    }

    private final MainMenu mainMenu;
    private final PlayerAuctionsMenu playerAuctionsMenu;
    private final BidMenu bidMenu;
    private final CreateAuctionMenu createAuctionMenu;

    @Inject
    public AuctionMenu(PluginManager pluginManager, Logger logger, Component displayName, AuctionService auctionService, CurrencyService currencyService) {
        super(pluginManager, logger, displayName, 54);
        this.auctionService = auctionService;
        this.currencyService = currencyService;
        this.mainMenu = new MainMenu(this);
        this.playerAuctionsMenu = new PlayerAuctionsMenu(this);
        this.bidMenu = new BidMenu(this);
        this.createAuctionMenu = new CreateAuctionMenu(this);
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
