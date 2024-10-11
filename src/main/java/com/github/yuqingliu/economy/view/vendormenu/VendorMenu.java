package com.github.yuqingliu.economy.view.vendormenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.view.vendormenu.mainmenu.MainMenu;
import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class VendorMenu extends AbstractPlayerInventory {
    protected final VendorService vendorService;
    protected final CurrencyService currencyService;
    protected MenuType currentMenu;

    protected enum MenuType {
        MainMenu, ItemMenu, TransactionMenu, TradeMenu;
    }

    @Inject
    public VendorMenu(EventManager eventManager, Component displayName, VendorService vendorService, CurrencyService currencyService) {
        super(
            eventManager,
            displayName,
            54
        );
        this.vendorService = vendorService;
        this.currencyService = currencyService;
    }
    
    protected String getVendorName() {
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
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, displayName);
        player.openInventory(inv);
        new MainMenu(eventManager, displayName, vendorService, currencyService).getController().openMainMenu(inv);
    }

}
