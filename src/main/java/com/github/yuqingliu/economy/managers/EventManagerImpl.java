package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.events.*;

@Component
public class EventManagerImpl implements EventManager {
    private Map<String, Listener> listeners = new HashMap<>();
    
    private final JavaPlugin plugin;

    @Autowired
    public EventManagerImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        initializeListeners();
        registerEvents();
    }

    private void initializeListeners() {
        listeners.put(PlayerJoinsServer.class.getSimpleName(), new PlayerJoinsServer());
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
