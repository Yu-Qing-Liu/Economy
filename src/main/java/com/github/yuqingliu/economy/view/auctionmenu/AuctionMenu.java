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
import com.google.inject.Inject;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class AuctionMenu extends AbstractPlayerInventory {
    private final AuctionService auctionService;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu;
    }

    private final MainMenu mainMenu;

    @Inject
    public AuctionMenu(PluginManager pluginManager, Logger logger, Component displayName, AuctionService auctionService) {
        super(pluginManager, logger, displayName, 54);
        this.auctionService = auctionService;
        this.mainMenu = new MainMenu(this);
    }
    
    public String getBankName() {
        return componentToString(displayName);
    }

    private String componentToString(Component component) {
        if(component == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
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
