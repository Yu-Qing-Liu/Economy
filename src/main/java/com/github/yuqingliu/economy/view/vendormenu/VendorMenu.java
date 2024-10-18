package com.github.yuqingliu.economy.view.vendormenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.view.vendormenu.itemmenu.ItemMenu;
import com.github.yuqingliu.economy.view.vendormenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.vendormenu.trademenu.TradeMenu;
import com.github.yuqingliu.economy.view.vendormenu.transactionmenu.TransactionMenu;
import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
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
        MainMenu, ItemMenu, TransactionMenu, TradeMenu;
    }

    private final MainMenu mainMenu;
    private final ItemMenu itemMenu;
    private final TransactionMenu transactionMenu;
    private final TradeMenu tradeMenu;

    @Inject
    public VendorMenu(EventManager eventManager, SoundManager soundManager, Logger logger, Component displayName, VendorService vendorService, CurrencyService currencyService) {
        super(
            eventManager,
            soundManager,
            logger,
            displayName,
            54
        );
        this.vendorService = vendorService;
        this.currencyService = currencyService;
        this.mainMenu = new MainMenu(this);
        this.itemMenu = new ItemMenu(this);
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
    public void load(Player player) {
        inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
    } 

    @Override
    public void open(Player player) {
        inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        mainMenu.getController().openMainMenu(inventory, player);
    }
}
