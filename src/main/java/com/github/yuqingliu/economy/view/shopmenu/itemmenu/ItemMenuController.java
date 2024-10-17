package com.github.yuqingliu.economy.view.shopmenu.itemmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class ItemMenuController {
    private final ShopMenu shopMenu;
    private final int prevPagePtr = 16;
    private final int nextPagePtr = 43;
    private final int prev = 25;
    private final int exit = 34;
    private final int length = 24;
    private Material voidOption = Material.GLASS_PANE;
    private final List<Integer> options = Arrays.asList(10,11,12,13,14,15,19,20,21,22,23,24,28,29,30,31,32,33,37,38,39,40,41,42);
    private final List<Integer> buttons = Arrays.asList(16,43,25,34);
    private Map<Integer, ShopItemEntity[]> pageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> pageNumbers = new ConcurrentHashMap<>();
    private ShopSectionEntity section;

    public ItemMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }

    public void openItemMenu(Inventory inv, ShopSectionEntity section, Player player) {
        this.section = section;
        pageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.ItemMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
        frame(inv);
        pagePtrs(inv);
        Scheduler.runAsync((task) -> {
            fetchItems();
            displayItems(inv, player);
        });
    }

    public void nextPage(Inventory inv, Player player) {
        pageNumbers.get(player)[0]++;
        if(pageData.containsKey(pageNumbers.get(player)[0])) {
            displayItems(inv, player);
        } else {
            pageNumbers.get(player)[0]--;
        }     
    }

    public void prevPage(Inventory inv, Player player) {
        pageNumbers.get(player)[0]--;
        if(pageNumbers.get(player)[0] > 0) {
            displayItems(inv, player);
        } else {
            pageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        pageNumbers.remove(player);
    }

    private void fetchItems() {
        pageData.clear();
        Set<ShopItemEntity> items = section.getItems();
        if(items.isEmpty()) {
            return;
        }
        Queue<ShopItemEntity> temp = new ArrayDeque<>();
        temp.addAll(items);
        int maxPages = (int) Math.ceil((double) items.size() / (double) length);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            ShopItemEntity[] options = new ShopItemEntity[length];
            for (int j = 0; j < length; j++) {
                if(temp.isEmpty()) {
                    options[j] = null;
                } else {
                    options[j] = temp.poll();
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayItems(Inventory inv, Player player) {
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        ShopItemEntity[] items = pageData.getOrDefault(pageNumbers.get(player)[0], new ShopItemEntity[length]);
        int currentIndex = 0;
        for(int i : options) {
            if(items[currentIndex] == null) {
                inv.setItem(i, Placeholder);
            } else {
                ItemStack item = items[currentIndex].getIcon().clone(); 
                inv.setItem(i, item);
            }
            currentIndex++;
        }
    }

    private void frame(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = Placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(meta);
        for (int i = 0; i < shopMenu.getInventorySize(); i++) {
            inv.setItem(i, Placeholder);
        }
    }

    private void pagePtrs(Inventory inv) {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nmeta = nextPage.getItemMeta();
        if(nmeta != null) {
            nmeta.displayName(Component.text("Next Page", NamedTextColor.AQUA));
        }
        nextPage.setItemMeta(nmeta);
        inv.setItem(nextPagePtr, nextPage);

        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta pmeta = prevPage.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Previous Page", NamedTextColor.AQUA));
        }
        prevPage.setItemMeta(pmeta);
        inv.setItem(prevPagePtr, prevPage);

        ItemStack prev = new ItemStack(Material.GREEN_WOOL);
        ItemMeta prevmeta = prev.getItemMeta();
        if(prevmeta != null) {
            prevmeta.displayName(Component.text("Sections", NamedTextColor.GRAY));
        }
        prev.setItemMeta(prevmeta);
        inv.setItem(this.prev, prev);

        ItemStack exit = new ItemStack(Material.RED_WOOL);
        ItemMeta emeta = exit.getItemMeta();
        if(emeta != null) {
            emeta.displayName(Component.text("Exit", NamedTextColor.RED));
        }
        exit.setItemMeta(emeta);
        inv.setItem(this.exit, exit);
    }
}
