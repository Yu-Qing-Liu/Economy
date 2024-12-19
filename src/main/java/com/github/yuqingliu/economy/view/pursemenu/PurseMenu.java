package com.github.yuqingliu.economy.view.pursemenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.view.pursemenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.pursemenu.mainmenu.MainMenuController;
import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;

import net.kyori.adventure.text.Component;

@Getter
public class PurseMenu extends AbstractPlayerInventory {
    private final CurrencyService currencyService;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu;
    }

    private final MainMenu mainMenu;

    @Inject
    public PurseMenu(PluginManager pluginManager, Logger logger, Component displayName, CurrencyService currencyService) {
        super(
            pluginManager,
            logger,
            displayName,
            27
        );
        this.currencyService = currencyService;
        this.mainMenu = new MainMenu(this);
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
