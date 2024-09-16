package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.events.*;
import com.github.yuqingliu.economy.persistence.services.PlayerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EventManagerImpl implements EventManager {
    private Map<String, Listener> listeners = new HashMap<>();
    private final JavaPlugin plugin;
    private final PlayerService playerService;
    
    @Inject
    public EventManagerImpl(JavaPlugin plugin, PlayerService playerService) {
        this.plugin = plugin;
        this.playerService = playerService;
        initializeListeners();
        registerEvents();
    }

    private void initializeListeners() {
        listeners.put(PlayerJoinsServer.class.getSimpleName(), new PlayerJoinsServer(playerService));
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
