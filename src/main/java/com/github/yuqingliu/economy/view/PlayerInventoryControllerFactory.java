package com.github.yuqingliu.economy.view;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

public class PlayerInventoryControllerFactory<T> {
    private final Map<Player, T> controllers = new ConcurrentHashMap<>();
    
    public T getPlayerInventoryController(Player player, T controller) {
        return controllers.computeIfAbsent(player, p -> controller);
    }

    public void removePlayerInventoryController(Player player) {
        this.controllers.remove(player);
    }
}
