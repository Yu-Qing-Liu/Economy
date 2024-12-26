package com.github.yuqingliu.economy.view;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public abstract class AbstractPlayerInventoryController<T> {
    protected final Player player;
    protected Inventory inventory;
    protected final T menu;
    protected int inventoryLength = 9;
    protected int inventorySize;
    
    public AbstractPlayerInventoryController(Player player, Inventory inventory, T menu) {
        this.player = player;
        this.inventory = inventory;
        this.menu = menu;
        this.inventorySize = inventory.getSize();
    }

    protected Component unavailableComponent = Component.text("Unavailable", NamedTextColor.DARK_PURPLE);
    protected Component loadingComponent = Component.text("Loading...", NamedTextColor.RED);
    protected ItemStack nextPageIcon = createSlotItem(Material.ARROW, Component.text("Next Page", NamedTextColor.AQUA));;
    protected ItemStack prevPageIcon = createSlotItem(Material.ARROW, Component.text("Previous Page", NamedTextColor.AQUA));
    protected ItemStack prevMenuIcon = createSlotItem(Material.GREEN_WOOL, Component.text("Previous Menu", NamedTextColor.GREEN));
    protected ItemStack exitMenuIcon = createSlotItem(Material.RED_WOOL, Component.text("Exit", NamedTextColor.RED));
    protected ItemStack reloadIcon = createSlotItem(Material.YELLOW_WOOL, Component.text("Refresh", NamedTextColor.YELLOW));
    protected ItemStack unavailableIcon = createSlotItem(Material.GLASS_PANE, unavailableComponent);
    protected ItemStack loadingIcon = createSlotItem(Material.BARRIER, loadingComponent);

    public boolean isUnavailable(ItemStack item) {
        return item.isSimilar(unavailableIcon) || item.isSimilar(loadingIcon) || item.isSimilar(getBackgroundTile(item.getType()));
    }

    public String durationToString(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d days: %02d hrs: %02d mins: %02d s", days, hours, minutes, seconds);
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

    public ItemStack getBackgroundTile(Material material) {
        return createSlotItem(material, unavailableComponent);
    }

    public int[] toCoords(int slot) {
        int x = slot % inventoryLength;
        int y = slot / inventoryLength;
        return new int[] { x, y };
    }

    public void setItem(int[] coords, ItemStack item) {
        inventory.setItem(coords[0] + (coords[1] * inventoryLength), item);
    }

    public void setItem(List<Integer> coords, ItemStack item) {
        inventory.setItem(coords.get(0) + (coords.get(1) * inventoryLength), item);
    }

    public void clear() {
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, new ItemStack(Material.AIR));
        }
    }

    public void fill(ItemStack background) {
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, background);
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

    public int rectangleIndex(int[] coords, List<int[]> rectangle) {
        int index = 0;
        for(int[] coord : rectangle) {
            if(Arrays.equals(coord, coords)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void fillRectangleArea(int[] start, int width, int length, ItemStack item) {
        List<int[]> rectangleCoords = rectangleArea(start, width, length);
        for(int[] coords : rectangleCoords) {
            setItem(coords, item);
        }
    }

    public void rectangleAreaLoading(int[] start, int width, int length) {
        List<int[]> rectangleCoords = rectangleArea(start, width, length);
        setItem(rectangleCoords.get(0), loadingIcon);
        for (int i = 1; i < rectangleCoords.size(); i++) {
            setItem(rectangleCoords.get(i), unavailableIcon);
        }
    }
}
