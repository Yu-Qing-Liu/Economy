package com.github.yuqingliu.economy.view.shopmenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController {
    private final ShopMenu shopMenu;
    private final int[] prevSectionsButton = new int[]{0,0};
    private final int[] nextSectionsButton = new int[]{0,5};
    private final int[] prevItemsButton = new int[]{2,0};
    private final int[] nextItemsButton = new int[]{2,5};
    private final int[] prevMenuButton = new int[]{1,0};
    private final int[] exitMenuButton = new int[]{1,5};
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
    private Map<Integer, Map<List<Integer>, ShopSectionEntity>> pageSectionData = new ConcurrentHashMap<>();
    private Map<Integer, Map<List<Integer>, ShopItemEntity>> pageItemData = new ConcurrentHashMap<>();
    private Map<Player, int[]> sectionPageNumbers = new ConcurrentHashMap<>();
    private Map<Player, int[]> itemPageNumbers = new ConcurrentHashMap<>();

    public MainMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.sectionsOptions = shopMenu.rectangleArea(sectionsStart, sectionsWidth, sectionsLength);
        this.itemsOptions = shopMenu.rectangleArea(itemsStart, itemsWidth, itemsLength);
    }

    public void openMainMenu(Inventory inv, Player player) {
        sectionPageNumbers.put(player, new int[]{1});
        itemPageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        shopMenu.fill(inv, shopMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        buttons(inv);
        border(inv);
        shopMenu.rectangleAreaLoading(inv, sectionsStart, sectionsWidth, sectionsLength);
        shopMenu.rectangleAreaLoading(inv, itemsStart, itemsWidth, itemsLength);
        Scheduler.runAsync((task) -> {
            fetchSections();
            displaySections(inv, player);
            fetchItems(inv, player, sectionsStart);
            displayItems(inv, player);
        });
    }

    public void nextSectionPage(Inventory inv, Player player) {
        sectionPageNumbers.get(player)[0]++;
        if(pageSectionData.containsKey(sectionPageNumbers.get(player)[0])) {
            displaySections(inv, player);
        } else {
            sectionPageNumbers.get(player)[0]--;
        }     
    }

    public void prevSectionPage(Inventory inv, Player player) {
        sectionPageNumbers.get(player)[0]--;
        if(sectionPageNumbers.get(player)[0] > 0) {
            displaySections(inv, player);
        } else {
            sectionPageNumbers.get(player)[0]++;
        }
    }

    public void nextItemPage(Inventory inv, Player player) {
        itemPageNumbers.get(player)[0]++;
        if(pageItemData.containsKey(itemPageNumbers.get(player)[0])) {
            displayItems(inv, player);
        } else {
            itemPageNumbers.get(player)[0]--;
        }     
    }

    public void prevItemPage(Inventory inv, Player player) {
        itemPageNumbers.get(player)[0]--;
        if(itemPageNumbers.get(player)[0] > 0) {
            displayItems(inv, player);
        } else {
            itemPageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        sectionPageNumbers.remove(player);
        itemPageNumbers.remove(player);
    }

    private void border(Inventory inv) {
        ItemStack borderItem = shopMenu.createSlotItem(Material.CHAIN, shopMenu.getUnavailableComponent());
        shopMenu.fillRectangleArea(inv, new int[]{2,1}, 4, 1, borderItem);
        shopMenu.fillRectangleArea(inv, new int[]{0,1}, 4, 1, borderItem);
    }

    private void buttons(Inventory inv) {
        shopMenu.setItem(inv, prevSectionsButton, shopMenu.getPrevPage());
        shopMenu.setItem(inv, nextSectionsButton, shopMenu.getNextPage());
        shopMenu.setItem(inv, prevItemsButton, shopMenu.getPrevPage());
        shopMenu.setItem(inv, nextItemsButton, shopMenu.getNextPage());
        shopMenu.setItem(inv, prevMenuButton, shopMenu.getPrevMenu());
        shopMenu.setItem(inv, exitMenuButton, shopMenu.getExitMenu());
        shopMenu.setItem(inv, buyOrdersMenuButton, shopMenu.createSlotItem(Material.ENDER_CHEST, Component.text("Buy Orders", NamedTextColor.LIGHT_PURPLE)));
        shopMenu.setItem(inv, sellOrdersMenuButton, shopMenu.createSlotItem(Material.ENDER_CHEST, Component.text("Sell Orders", NamedTextColor.LIGHT_PURPLE)));
    }

    private void fetchSections() {
        pageSectionData.clear();
        ShopEntity vendor = shopMenu.getShopService().getShop(shopMenu.getShopName()); 
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
            Map<List<Integer>, ShopSectionEntity> options = new HashMap<>();
            for (int[] coords : sectionsOptions) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageSectionData.put(pageNum, options);
        }
    }

    private void fetchItems(Inventory inv, Player player, int[] sectionCoords) {
        pageItemData.clear();
        Map<List<Integer>, ShopSectionEntity> sections = pageSectionData.getOrDefault(sectionPageNumbers.get(player)[0], Collections.emptyMap());
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
            Map<List<Integer>, ShopItemEntity> options = new HashMap<>();
            for (int[] coords : itemsOptions) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageItemData.put(pageNum, options);
        }
    }

    private void displaySections(Inventory inv, Player player) {
        Map<List<Integer>, ShopSectionEntity> sections = pageSectionData.getOrDefault(sectionPageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, ShopSectionEntity> entry : sections.entrySet()) {
            List<Integer> coords = entry.getKey();
            ShopSectionEntity section = entry.getValue();
            if(section == null) {
                shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
            } else {
                ItemStack item = section.getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    Component description = Component.text("Section", NamedTextColor.GRAY);
                    meta.lore(Arrays.asList(description));
                    item.setItemMeta(meta);
                }
                shopMenu.setItem(inv, coords, item);
            }
        }
    }

    public void displayInitialItems(Inventory inv, Player player, int[] sectionCoords) {
        shopMenu.rectangleAreaLoading(inv, itemsStart, itemsWidth, itemsLength);
        Scheduler.runAsync((task) -> {
            fetchItems(inv, player, sectionCoords);
            Map<List<Integer>, ShopItemEntity> items = pageItemData.getOrDefault(itemPageNumbers.get(player)[0], Collections.emptyMap());
            for(Map.Entry<List<Integer>, ShopItemEntity> entry : items.entrySet()) {
                List<Integer> coords = entry.getKey();
                ShopItemEntity item = entry.getValue();
                if(item == null) {
                    shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
                } else {
                    ItemStack icon = item.getIcon().clone();
                    shopMenu.setItem(inv, coords, icon);
                }
            }
        });
    }

    public void displayItems(Inventory inv, Player player) {
        Map<List<Integer>, ShopItemEntity> items = pageItemData.getOrDefault(itemPageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, ShopItemEntity> entry : items.entrySet()) {
            List<Integer> coords = entry.getKey();
            ShopItemEntity item = entry.getValue();
            if(item == null) {
                shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
            } else {
                ItemStack icon = item.getIcon().clone();
                shopMenu.setItem(inv, coords, icon);
            }
        }
    }
}
