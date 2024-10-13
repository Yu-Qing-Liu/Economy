package com.github.yuqingliu.economy.view;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

@Getter
public abstract class AbstractPlayerInventory implements PlayerInventory {
    protected EventManager eventManager;
    @Setter protected Component displayName;
    protected final int inventorySize;
        
    @Inject
    public AbstractPlayerInventory(EventManager eventManager, Component displayName, int inventorySize) {
        this.eventManager = eventManager;
        this.displayName = displayName;
        this.inventorySize = inventorySize;
    }

    public void clear(Inventory inv) {
        for (int i = 0; i < inventorySize; i++) {
            inv.setItem(i, new ItemStack(Material.AIR));
        }
    }

    public void addItemToPlayer(Player player, ItemStack item, int quantity) {
        item.setAmount(quantity);
        if (!player.getInventory().addItem(item).isEmpty()) {
            Location location = player.getLocation();
            player.getWorld().dropItemNaturally(location, item);
        }
    }

    public boolean removeItemToPlayer(Player player, ItemStack item, int quantity) {
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                if(inventoryItem.getAmount() >= quantity) {
                    inventoryItem.setAmount(inventoryItem.getAmount() - quantity);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public abstract void open(Player player);
}
