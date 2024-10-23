package com.github.yuqingliu.economy.view;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public abstract class AbstractPlayerInventory implements PlayerInventory {
    protected final int inventoryLength = 9;
    protected EventManager eventManager;
    protected final SoundManager soundManager;
    protected final Logger logger;
    @Setter protected Component displayName;
    protected final int inventorySize;
    protected ItemStack[][] grid;
    protected Map<Material, ItemStack> backgroundItems = new HashMap<>();   
    protected ItemStack nextPage;
    protected ItemStack prevPage;
    protected ItemStack prevMenu;
    protected ItemStack exitMenu;
    protected ItemStack unavailable;
    protected ItemStack loading;
        
    @Inject
    public AbstractPlayerInventory(EventManager eventManager, SoundManager soundManager, Logger logger, Component displayName, int inventorySize) {
        this.eventManager = eventManager;
        this.soundManager = soundManager;
        this.logger = logger;
        this.displayName = displayName;
        this.inventorySize = inventorySize;
        this.grid = new ItemStack[inventoryLength][inventorySize/inventoryLength];
        initializeBackgroundItems();
    }

    private void initializeBackgroundItems() {
        Component backgroundText = Component.text("UNAVAILABLE", NamedTextColor.DARK_PURPLE);
        for(Material material : Material.values()) {
            if(material.name().contains("STAINED_GLASS_PANE")) {
                backgroundItems.put(material, createSlotItem(material, backgroundText));
            }
        }
    }

    public ItemStack createSlotItem(Material material, Component displayName, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.displayName(displayName);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSlotItem(Material material, Component displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.displayName(displayName);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.lore(Collections.emptyList());
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSlotItem(Material material, Component displayName, Component lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.displayName(displayName);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.lore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public void initializeGrid(ItemStack backGroundItem) {
        for(int i = 0; i < inventoryLength; i++) {
            for (int j = 0; j < inventorySize/inventoryLength; j++) {
                grid[i][j] = backGroundItem;
            }
        }
    }

    public void renderGrid(Inventory inv) {
        for(int i = 0; i < inventoryLength; i++) {
            for (int j = 0; j < inventorySize/inventoryLength; j++) {
                inv.setItem(i + (j * inventoryLength), grid[i][j]);
            }
        }
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
