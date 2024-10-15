package com.github.yuqingliu.economy.view.shopmenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.shopmenu.mainmenu.MainMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class ShopMenu extends AbstractPlayerInventory {
    protected final ShopService shopService;
    protected final CurrencyService currencyService;
    @Setter protected MenuType currentMenu;

    public enum MenuType {
        MainMenu, ItemMenu, TransactionMenu, TradeMenu;
    }

    protected final MainMenu mainMenu;

    @Inject
    public ShopMenu(EventManager eventManager, Component displayName, ShopService shopService, CurrencyService currencyService) {
        super(
            eventManager,
            displayName,
            54
        );
        this.shopService = shopService;
        this.currencyService = currencyService;
        this.mainMenu = new MainMenu(this);
    }
    
    public String getShopName() {
        return componentToString(displayName);
    }

    private String componentToString(Component component) {
        if(component == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inv);
        mainMenu.getController().openMainMenu(inv);
    }
}
