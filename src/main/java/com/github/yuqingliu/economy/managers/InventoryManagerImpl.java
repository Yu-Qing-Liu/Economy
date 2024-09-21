package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.Listener;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Singleton
public class InventoryManagerImpl implements InventoryManager {
    private final EventManager eventManager;
    private final CurrencyService currencyService;
    private Map<String, AbstractPlayerInventory> inventories = new HashMap<>();
    
    @Inject
    public InventoryManagerImpl(EventManager eventManager, CurrencyService currencyService) {
        this.eventManager = eventManager;
        this.currencyService = currencyService;
        initializeInventories();
    }

    private void initializeInventories() {
        inventories.put(
            PurseMenu.class.getSimpleName(),
            new PurseMenu(
                eventManager,
                Component.text("Purse", NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                currencyService
            )
        );
    }

    @Override
    public PlayerInventory getInventory(String className) {
        return inventories.get(className);
    }
}
