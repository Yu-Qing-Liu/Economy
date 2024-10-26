package com.github.yuqingliu.economy.view.bankmenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.persistence.services.BankService;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.bankmenu.accountmenu.AccountMenu;
import com.github.yuqingliu.economy.view.bankmenu.mainmenu.MainMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class BankMenu extends AbstractPlayerInventory {
    private final BankService bankService;
    private final CurrencyService currencyService;
    private final InventoryManager inventoryManager;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu, AccountMenu;
    }

    private final MainMenu mainMenu;
    private final AccountMenu accountMenu;

    @Inject
    public BankMenu(EventManager eventManager, SoundManager soundManager, Logger logger, Component displayName, BankService bankService, CurrencyService currencyService, InventoryManager inventoryManager) {
        super(
            eventManager,
            soundManager,
            logger,
            displayName,
            27
        );
        this.bankService = bankService;
        this.currencyService = currencyService;
        this.inventoryManager = inventoryManager;
        this.mainMenu = new MainMenu(this);
        this.accountMenu = new AccountMenu(this);
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
        mainMenu.getController().openMainMenu(player, inventory);
    }
}
