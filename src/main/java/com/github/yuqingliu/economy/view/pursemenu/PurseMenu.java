package com.github.yuqingliu.economy.view.pursemenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.view.pursemenu.mainmenu.MainMenu;
import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;

import net.kyori.adventure.text.Component;

@Getter
public class PurseMenu extends AbstractPlayerInventory {
    protected final CurrencyService currencyService;
    protected MenuType currentMenu;

    protected enum MenuType {
        MainMenu;
    }

    @Inject
    public PurseMenu(EventManager eventManager, Component displayName, CurrencyService currencyService) {
        super(
            eventManager,
            displayName,
            27
        );
        this.currencyService = currencyService;
        this.currentMenu = MenuType.MainMenu;
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, displayName);
        player.openInventory(inv);
        new MainMenu(eventManager, displayName, currencyService).getController().openMainMenu(player, inv);
    }
}
