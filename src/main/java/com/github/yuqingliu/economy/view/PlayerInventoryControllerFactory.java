package com.github.yuqingliu.economy.view;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

public class PlayerInventoryControllerFactory<T> {
    private final Map<Player, T> controllers = new ConcurrentHashMap<>();
    
    public T getPlayerInventoryController(Player player, T controller) {
        AbstractPlayerInventoryController oldC = (AbstractPlayerInventoryController) controllers.get(player);
        AbstractPlayerInventoryController newC = (AbstractPlayerInventoryController) controller;
        if(oldC != null && oldC.getInventory().equals(newC.getInventory())) {
            return controllers.get(player);
        } else {
            controllers.put(player, controller);
            return controller;
        }
    }

    public void removePlayerInventoryController(Player player) {
        this.controllers.remove(player);
    }
}
