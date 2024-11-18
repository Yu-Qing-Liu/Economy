package com.github.yuqingliu.economy.view;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

public class InventoryControllerFactory<T> {
    private final Map<Player, T> controllers = new ConcurrentHashMap<>();
    
    public T computeIfAbsent(Player player, T controller) {
        return controllers.computeIfAbsent(player, p -> controller);
    }

    public void removeController(Player player) {
        this.controllers.remove(player);
    }
}
