package com.github.yuqingliu.economy.view;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

@Getter
public abstract class AbstractPlayerInventory implements PlayerInventory {
    protected EventManager eventManager;
    @Setter protected Component displayName;
    protected final int INVENTORY_SIZE;
        
    @Inject
    public AbstractPlayerInventory(EventManager eventManager, Component displayName, int INVENTORY_SIZE) {
        this.eventManager = eventManager;
        this.displayName = displayName;
        this.INVENTORY_SIZE = INVENTORY_SIZE;
    }

    protected void clear(Inventory inv) {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inv.setItem(i, new ItemStack(Material.AIR));
        }
    }

    @Override
    public abstract void open(Player player);
}
