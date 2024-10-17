package com.github.yuqingliu.economy.api.view;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.kyori.adventure.text.Component;

public interface PlayerInventory {
    Inventory getInventory();
    void open(Player player);
    void load(Player player);
    void setDisplayName(Component displayName);
}
