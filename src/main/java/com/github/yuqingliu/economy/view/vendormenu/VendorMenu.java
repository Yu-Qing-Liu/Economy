package com.github.yuqingliu.economy.view.vendormenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.view.vendormenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.vendormenu.trademenu.TradeMenu;
import com.github.yuqingliu.economy.view.vendormenu.transactionmenu.TransactionMenu;
import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class VendorMenu extends AbstractPlayerInventory {
    private final VendorService vendorService;
    private final CurrencyService currencyService;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu, TransactionMenu, TradeMenu;
    }

    private final MainMenu mainMenu;
    private final TransactionMenu transactionMenu;
    private final TradeMenu tradeMenu;

    @Inject
    public VendorMenu(PluginManager pluginManager, Logger logger, Component displayName, VendorService vendorService, CurrencyService currencyService) {
        super(
            pluginManager,
            logger,
            displayName,
            54
        );
        this.vendorService = vendorService;
        this.currencyService = currencyService;
        this.mainMenu = new MainMenu(this);
        this.transactionMenu = new TransactionMenu(this);
        this.tradeMenu = new TradeMenu(this);
    }
    
    public String getVendorName() {
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
        mainMenu.getController().openMainMenu(inventory, player);
    }
}
