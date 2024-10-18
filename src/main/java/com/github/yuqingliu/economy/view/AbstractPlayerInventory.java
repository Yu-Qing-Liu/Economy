package com.github.yuqingliu.economy.view;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
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
    protected Inventory inventory;
    protected EventManager eventManager;
    protected final SoundManager soundManager;
    protected final Logger logger;
    @Setter protected Component displayName;
    protected final int inventorySize;
        
    @Inject
    public AbstractPlayerInventory(EventManager eventManager, SoundManager soundManager, Logger logger, Component displayName, int inventorySize) {
        this.eventManager = eventManager;
        this.soundManager = soundManager;
        this.logger = logger;
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
        int totalItemCount = countItemToPlayer(player, item);
        if (totalItemCount < quantity) {
            return false;
        }
        int remaining = quantity;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                int amount = inventoryItem.getAmount();
                if (amount >= remaining) {
                    inventoryItem.setAmount(amount - remaining);
                    return true;
                } else {
                    inventoryItem.setAmount(0);
                    remaining -= amount;
                }
            }
        }
        return false;
    }

    public int countItemToPlayer(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                count += inventoryItem.getAmount();
            }
        }
        return count;
    }

    @Override
    public abstract void load(Player player);

    @Override
    public abstract void open(Player player);

}
