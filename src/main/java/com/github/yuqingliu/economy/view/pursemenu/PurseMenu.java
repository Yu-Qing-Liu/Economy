package com.github.yuqingliu.economy.view.pursemenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.view.pursemenu.mainmenu.MainMenu;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;

import net.kyori.adventure.text.Component;

@Getter
public class PurseMenu extends AbstractPlayerInventory {
    protected final CurrencyService currencyService;
    @Setter protected MenuType currentMenu;

    public enum MenuType {
        MainMenu;
    }

    protected final MainMenu mainMenu;

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
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inv);
        mainMenu.getController().openMainMenu(player, inv);
    }
}
