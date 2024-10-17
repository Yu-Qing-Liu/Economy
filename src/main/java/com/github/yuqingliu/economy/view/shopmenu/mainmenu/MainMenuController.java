package com.github.yuqingliu.economy.view.shopmenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController {
    private final ShopMenu shopMenu;
    protected final int prevPagePtr = 16;
    protected final int nextPagePtr = 43;
    protected final int prev = 25;
    protected final int exit = 34;
    protected final int length = 24;
    protected Material voidOption = Material.GLASS_PANE;
    protected final List<Integer> options = Arrays.asList(10,11,12,13,14,15,19,20,21,22,23,24,28,29,30,31,32,33,37,38,39,40,41,42);
    protected final List<Integer> buttons = Arrays.asList(16,43,25,34);
    protected Map<Integer, ShopSectionEntity[]> pageData = new HashMap<>();
    protected int pageNumber = 1;

    public MainMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }

    public void openMainMenu(Inventory inv) {
        Scheduler.runLaterAsync((task) -> {
            shopMenu.setCurrentMenu(MenuType.MainMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
        frame(inv);
        pagePtrs(inv);
        Scheduler.runAsync((task) -> {
            fetchSections();
            displaySections(inv);
        });
    }

    public void nextPage(Inventory inv) {
        pageNumber++;
        if(pageData.containsKey(pageNumber)) {
            displaySections(inv);
        } else {
            pageNumber--;
        }     
    }

    public void prevPage(Inventory inv) {
        pageNumber--;
        if(pageNumber > 0) {
            displaySections(inv);
        } else {
            pageNumber++;
        }
    }

    public void onClose() {
        pageData.clear();
    }

    private void fetchSections() {
        ShopEntity shop = shopMenu.getShopService().getShop(shopMenu.getShopName()); 
        if(shop == null) {
            return;
        }
        Set<ShopSectionEntity> sections = shop.getSections();
        if(sections.isEmpty()) {
            return;
        }
        Queue<ShopSectionEntity> temp = new ArrayDeque<>();
        temp.addAll(sections);
        int maxPages = (int) Math.ceil((double) sections.size() / (double) length);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            ShopSectionEntity[] options = new ShopSectionEntity[length];
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

    private void displaySections(Inventory inv) {
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        ShopSectionEntity[] sections = pageData.getOrDefault(pageNumber, new ShopSectionEntity[length]);
        int currentIndex = 0;
        for(int i : options) {
            if(sections[currentIndex] == null) {
                inv.setItem(i, Placeholder);
            } else {
                ItemStack item = sections[currentIndex].getIcon().clone(); 
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    Component description = Component.text("Section", NamedTextColor.GRAY);
                    List<Component> lore = new ArrayList<>();
                    lore.add(description);
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(i, item);
            }
            currentIndex++;
        }
    }

    private void frame(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
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

        ItemStack prev = new ItemStack(Material.GRAY_WOOL);
        ItemMeta prevmeta = prev.getItemMeta();
        if(prevmeta != null) {
            prevmeta.displayName(Component.text("Unavailable", NamedTextColor.GRAY));
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
