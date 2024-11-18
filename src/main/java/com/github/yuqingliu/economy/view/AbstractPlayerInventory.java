package com.github.yuqingliu.economy.view;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.kyori.adventure.text.Component;

@Getter
public abstract class AbstractPlayerInventory implements PlayerInventory {
    protected final PluginManager pluginManager;
    protected final Logger logger;
    @Setter protected Component displayName;
    protected final int inventorySize;
       
    @Inject
    public AbstractPlayerInventory(PluginManager pluginManager, Logger logger, Component displayName, int inventorySize) {
        this.pluginManager = pluginManager;
        this.logger = logger;
        this.displayName = displayName;
        this.inventorySize = inventorySize;
    }

    @Override
    public abstract Inventory load(Player player);

    @Override
    public abstract void open(Player player);
}
