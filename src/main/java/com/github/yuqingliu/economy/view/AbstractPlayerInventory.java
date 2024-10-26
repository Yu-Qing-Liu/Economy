package com.github.yuqingliu.economy.view;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
    protected final Component unavailableComponent = Component.text("Unavailable", NamedTextColor.DARK_PURPLE);
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
        initializeBackgroundItems();
        initializeCommonItems();
    }

    private void initializeBackgroundItems() {
        for(Material material : Material.values()) {
            if(material.name().contains("STAINED_GLASS_PANE")) {
                backgroundItems.put(material, createSlotItem(material, unavailableComponent));
            }
        }
    }

    private void initializeCommonItems() {
        this.nextPage = createSlotItem(Material.ARROW, Component.text("Next Page", NamedTextColor.AQUA));
        this.prevPage = createSlotItem(Material.ARROW, Component.text("Previous Page", NamedTextColor.AQUA));
        this.prevMenu = createSlotItem(Material.GREEN_WOOL, Component.text("Previous Menu", NamedTextColor.GREEN));
        this.exitMenu = createSlotItem(Material.RED_WOOL, Component.text("Exit", NamedTextColor.RED));
        this.unavailable = createSlotItem(Material.GLASS_PANE, unavailableComponent);
        this.loading = createSlotItem(Material.BARRIER, Component.text("Loading...", NamedTextColor.RED));
    }

    public boolean isUnavailable(ItemStack item) {
        return item.displayName().equals(unavailableComponent);
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

    public int[] toCoords(int slot) {
        int x = slot % inventoryLength;
        int y = slot / inventoryLength;
        return new int[] { x, y };
    }

    public void setItem(Inventory inv, int[] coords, ItemStack item) {
        inv.setItem(coords[0] + (coords[1] * inventoryLength), item);
    }

    public void setItem(Inventory inv, List<Integer> coords, ItemStack item) {
        inv.setItem(coords.get(0) + (coords.get(1) * inventoryLength), item);
    }

    public void clear(Inventory inv) {
        for (int i = 0; i < inventorySize; i++) {
            inv.setItem(i, new ItemStack(Material.AIR));
        }
    }

    public void fill(Inventory inv, ItemStack background) {
        for (int i = 0; i < inventorySize; i++) {
            inv.setItem(i, background);
        }
    }

    public boolean rectangleContains(int[] coords, List<int[]> rectangle) {
        return rectangle.stream().anyMatch(coord -> Arrays.equals(coord, coords));
    }

    public List<int[]> rectangleArea(int[] start, int width, int length) {
        List<int[]> results = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                int[] current = new int[]{start[0] + j, start[1] + i};
                results.add(current);
            }
        }
        return results;
    }

    public void fillRectangleArea(Inventory inv, int[] start, int width, int length, ItemStack item) {
        List<int[]> rectangleCoords = rectangleArea(start, width, length);
        for(int[] coords : rectangleCoords) {
            setItem(inv, coords, item);
        }
    }

    public void rectangleAreaLoading(Inventory inv, int[] start, int width, int length) {
        List<int[]> rectangleCoords = rectangleArea(start, width, length);
        setItem(inv, rectangleCoords.get(0), loading);
        for (int i = 1; i < rectangleCoords.size(); i++) {
            setItem(inv, rectangleCoords.get(i), unavailable);
        }
    }

    public void addItemToPlayer(Player player, ItemStack item, int quantity) {
        item.setAmount(quantity);
        if (!player.getInventory().addItem(item).isEmpty()) {
            Location location = player.getLocation();
            player.getWorld().dropItemNaturally(location, item);
        }
    }

    public boolean removeItemFromPlayer(Player player, ItemStack item, int quantity) {
        int totalItemCount = countItemFromPlayer(player, item);
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

    public int countItemFromPlayer(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                count += inventoryItem.getAmount();
            }
        }
        return count;
    }

    @Override
    public abstract Inventory load(Player player);

    @Override
    public abstract void open(Player player);
}
