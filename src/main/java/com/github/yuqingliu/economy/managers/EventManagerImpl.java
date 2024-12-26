package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.github.yuqingliu.economy.events.*;
import com.github.yuqingliu.economy.persistence.services.PlayerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EventManagerImpl implements EventManager {
    private Map<String, Listener> listeners = new HashMap<>();
    private final JavaPlugin plugin;
    private final PlayerService playerService;
    private final NameSpacedKeyManager nameSpacedKeyManager;
    private final InventoryManager inventoryManager;
    
    @Inject
    public EventManagerImpl(JavaPlugin plugin, PlayerService playerService, NameSpacedKeyManager nameSpacedKeyManager, InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.playerService = playerService;
        this.nameSpacedKeyManager = nameSpacedKeyManager;
        this.inventoryManager = inventoryManager;
        initializeListeners();
        registerEvents();
    }

    private void initializeListeners() {
        listeners.put(PlayerJoinsServer.class.getSimpleName(), new PlayerJoinsServer(playerService));
        listeners.put(PlayerOpensVendor.class.getSimpleName(), new PlayerOpensVendor(nameSpacedKeyManager, inventoryManager));
        listeners.put(PlayerOpensShop.class.getSimpleName(), new PlayerOpensShop(nameSpacedKeyManager, inventoryManager));
        listeners.put(PlayerOpensBank.class.getSimpleName(), new PlayerOpensBank(nameSpacedKeyManager, inventoryManager));
        listeners.put(PlayerOpensAuctionHouse.class.getSimpleName(), new PlayerOpensAuctionHouse(nameSpacedKeyManager, inventoryManager));
    }
    
    public void registerEvents() {
        for(Listener listener : listeners.values()) {
            registerEvent(listener);
        }
    }

    @Override
    public Listener getEvent(String className) {
        return listeners.get(className);
    }
    
    @Override
    public void registerEvent(Listener listener) {
         plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
    
    @Override
    public void unregisterEvent(String className) {
        HandlerList.unregisterAll(listeners.get(className));;
    }
}
