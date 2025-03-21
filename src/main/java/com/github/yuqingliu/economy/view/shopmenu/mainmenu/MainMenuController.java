package com.github.yuqingliu.economy.view.shopmenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.PageData;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController extends AbstractPlayerInventoryController<ShopMenu> {
    private final int[] prevSectionsButton = new int[]{1,0};
    private final int[] nextSectionsButton = new int[]{1,5};
    private final int[] prevItemsButton = new int[]{6,0};
    private final int[] nextItemsButton = new int[]{7,0};
    private final int[] exitMenuButton = new int[]{4,0};
    private final int[] buyOrdersMenuButton = new int[]{4,5};
    private final int[] sellOrdersMenuButton = new int[]{6,5};
    private final int sectionsLength = 1;
    private final int sectionsWidth = 4;
    private final int itemsLength = 5;
    private final int itemsWidth = 4;
    private final int sectionsSize = sectionsLength * sectionsWidth;
    private final int itemsSize = itemsLength * itemsWidth;
    private final int[] sectionsStart = new int[]{1,1};
    private final int[] itemsStart = new int[]{3,1};
    private final List<int[]> sectionsOptions;
    private final List<int[]> itemsOptions;
    private final PageData<ShopSectionEntity> sectionsPageData = new PageData<>();
    private final PageData<ShopItemEntity> itemPageData = new PageData<>();

    public MainMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
        this.sectionsOptions = rectangleArea(sectionsStart, sectionsWidth, sectionsLength);
        this.itemsOptions = rectangleArea(itemsStart, itemsWidth, itemsLength);
    }

    public void openMenu() {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        buttons();
        border();
        rectangleAreaLoading(sectionsStart, sectionsWidth, sectionsLength);
        rectangleAreaLoading(itemsStart, itemsWidth, itemsLength);
        Scheduler.runAsync((task) -> {
            fetchSections();
            displaySections();
            fetchItems(sectionsStart);
            displayItems();
        });
    }

    public void nextSectionPage() {
        sectionsPageData.nextPage(() -> displaySections());
    }

    public void prevSectionPage() {
        sectionsPageData.prevPage(() -> displaySections());
    }

    public void nextItemPage() {
        itemPageData.nextPage(() -> displayItems());
    }

    public void prevItemPage() {
        itemPageData.prevPage(() -> displayItems());
    }

    private void buttons() {
        setItem(prevSectionsButton, getPrevPageIcon());
        setItem(nextSectionsButton, getNextPageIcon());
        setItem(prevItemsButton, getPrevPageIcon());
        setItem(nextItemsButton, getNextPageIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(buyOrdersMenuButton, createSlotItem(Material.ENDER_CHEST, Component.text("Buy Orders", NamedTextColor.LIGHT_PURPLE)));
        setItem(sellOrdersMenuButton, createSlotItem(Material.ENDER_CHEST, Component.text("Sell Orders", NamedTextColor.LIGHT_PURPLE)));
    }

    private void border() {
        fillRectangleArea(new int[]{0,1}, 4, 1, getBackgroundTile(Material.CHAIN));
        fillRectangleArea(new int[]{2,1}, 4, 1, getBackgroundTile(Material.CHAIN));
        fillRectangleArea(new int[]{8,1}, 4, 1, getBackgroundTile(Material.CHAIN));
        setItem(new int[] {0,0}, getBackgroundTile(Material.OAK_HANGING_SIGN));
        setItem(new int[] {2,0}, getBackgroundTile(Material.OAK_HANGING_SIGN));
        setItem(new int[] {0,5}, getBackgroundTile(Material.OAK_HANGING_SIGN));
        setItem(new int[] {2,5}, getBackgroundTile(Material.OAK_HANGING_SIGN));
        setItem(new int[] {8,0}, getBackgroundTile(Material.OAK_HANGING_SIGN));
        setItem(new int[] {8,5}, getBackgroundTile(Material.OAK_HANGING_SIGN));
    }

    private void fetchSections() {
        sectionsPageData.clear();
        ShopEntity vendor = menu.getShopService().getShop(menu.getShopName()); 
        if(vendor == null) {
            return;
        }
        Set<ShopSectionEntity> sections = vendor.getSections();
        if(sections.isEmpty()) {
            return;
        }
        Queue<ShopSectionEntity> temp = new ArrayDeque<>();
        temp.addAll(sections);
        int maxPages = (int) Math.ceil((double) sections.size() / (double) sectionsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, ShopSectionEntity> options = new LinkedHashMap<>();
            for (int[] coords : sectionsOptions) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            sectionsPageData.put(pageNum, options);
        }
    }

    private void fetchItems(int[] sectionCoords) {
        itemPageData.clear();
        Map<List<Integer>, ShopSectionEntity> sections = sectionsPageData.getCurrentPageData();
        Set<ShopItemEntity> items = Collections.emptySet();
        if(sections.containsKey(Arrays.asList(sectionCoords[0], sectionCoords[1]))) {
            items = sections.get(Arrays.asList(sectionCoords[0], sectionCoords[1])).getItems();
        }
        if(sections.isEmpty() || items.isEmpty()) {
            return;
        }
        Queue<ShopItemEntity> temp = new ArrayDeque<>();
        temp.addAll(items);
        int maxPages = (int) Math.ceil((double) items.size() / (double) itemsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, ShopItemEntity> options = new LinkedHashMap<>();
            for (int[] coords : itemsOptions) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            itemPageData.put(pageNum, options);
        }
    }

    private void displaySections() {
        Map<List<Integer>, ShopSectionEntity> sections = sectionsPageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, ShopSectionEntity> entry : sections.entrySet()) {
            List<Integer> coords = entry.getKey();
            ShopSectionEntity section = entry.getValue();
            if(section == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack item = section.getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    Component description = Component.text("Section", NamedTextColor.GRAY);
                    meta.lore(Arrays.asList(description));
                    item.setItemMeta(meta);
                }
                setItem(coords, item);
            }
        }
    }

    public void displayInitialItems(Inventory inv, Player player, int[] sectionCoords) {
        rectangleAreaLoading(itemsStart, itemsWidth, itemsLength);
        Scheduler.runAsync((task) -> {
            fetchItems(sectionCoords);
            Map<List<Integer>, ShopItemEntity> items = itemPageData.getCurrentPageData();
            for(Map.Entry<List<Integer>, ShopItemEntity> entry : items.entrySet()) {
                List<Integer> coords = entry.getKey();
                ShopItemEntity item = entry.getValue();
                if(item == null) {
                    setItem(coords, getUnavailableIcon());
                } else {
                    ItemStack icon = item.getIcon().clone();
                    setItem(coords, icon);
                }
            }
        });
    }

    public void displayItems() {
        Map<List<Integer>, ShopItemEntity> items = itemPageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, ShopItemEntity> entry : items.entrySet()) {
            List<Integer> coords = entry.getKey();
            ShopItemEntity item = entry.getValue();
            if(item == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack icon = item.getIcon().clone();
                setItem(coords, icon);
            }
        }
    }
}
