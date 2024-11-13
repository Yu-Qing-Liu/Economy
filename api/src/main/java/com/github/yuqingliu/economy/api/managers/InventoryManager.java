package com.github.yuqingliu.economy.api.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.view.PlayerInventory;

public interface InventoryManager {
    PlayerInventory getInventory(String className);
    void addItemToPlayer(Player player, ItemStack item, int quantity);
    boolean removeItemFromPlayer(Player player, ItemStack item, int quantity);
    int countItemFromPlayer(Player player, ItemStack item);
    int countAvailableInventorySpace(Player player, Material material);
    void postConstruct();
}
