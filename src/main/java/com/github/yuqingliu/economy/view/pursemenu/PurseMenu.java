package com.github.yuqingliu.economy.view.pursemenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.view.pursemenu.mainmenu.MainMenu;
import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.managers.EventManager;
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
    public PurseMenu(EventManager eventManager, Component displayName, CurrencyService currencyService) {
        super(
            eventManager,
            displayName,
            27
        );
        this.currencyService = currencyService;
        this.mainMenu = new MainMenu(this);
    }

    @Override
    public void load(Player player) {
        inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
    }

    @Override
    public void open(Player player) {
        inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        mainMenu.getController().openMainMenu(player, inventory);
    }

}
