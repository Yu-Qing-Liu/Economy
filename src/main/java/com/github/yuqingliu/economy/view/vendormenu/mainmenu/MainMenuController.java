package com.github.yuqingliu.economy.view.vendormenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import com.github.yuqingliu.economy.persistence.entities.VendorEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorSectionEntity;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController {
    private final VendorMenu vendorMenu;
    private final int[] prevSectionsButton = new int[]{0,0};
    private final int[] nextSectionsButton = new int[]{0,5};
    private final int[] prevItemsButton = new int[]{2,0};
    private final int[] nextItemsButton = new int[]{2,5};
    private final int[] prevMenuButton = new int[]{1,0};
    private final int[] exitMenuButton = new int[]{1,5};
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
    private Map<Integer, Map<List<Integer>, VendorSectionEntity>> pageSectionData = new ConcurrentHashMap<>();
    private Map<Integer, Map<List<Integer>, VendorItemEntity>> pageItemData = new ConcurrentHashMap<>();
    private Map<Player, int[]> sectionPageNumbers = new ConcurrentHashMap<>();
    private Map<Player, int[]> itemPageNumbers = new ConcurrentHashMap<>();

    public MainMenuController(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.sectionsOptions = vendorMenu.rectangleArea(sectionsStart, sectionsWidth, sectionsLength);
        this.itemsOptions = vendorMenu.rectangleArea(itemsStart, itemsWidth, itemsLength);
    }

    public void openMainMenu(Inventory inv, Player player) {
        sectionPageNumbers.put(player, new int[]{1});
        itemPageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            vendorMenu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        vendorMenu.fill(inv, vendorMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        buttons(inv);
        border(inv);
        vendorMenu.rectangleAreaLoading(inv, sectionsStart, sectionsWidth, sectionsLength);
        vendorMenu.rectangleAreaLoading(inv, itemsStart, itemsWidth, itemsLength);
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
        ItemStack borderItem = vendorMenu.createSlotItem(Material.CHAIN, vendorMenu.getUnavailableComponent());
        vendorMenu.fillRectangleArea(inv, new int[]{2,1}, 4, 1, borderItem);
        vendorMenu.fillRectangleArea(inv, new int[]{0,1}, 4, 1, borderItem);
    }

    private void buttons(Inventory inv) {
        vendorMenu.setItem(inv, prevSectionsButton, vendorMenu.getPrevPage());
        vendorMenu.setItem(inv, nextSectionsButton, vendorMenu.getNextPage());
        vendorMenu.setItem(inv, prevItemsButton, vendorMenu.getPrevPage());
        vendorMenu.setItem(inv, nextItemsButton, vendorMenu.getNextPage());
        vendorMenu.setItem(inv, prevMenuButton, vendorMenu.getPrevMenu());
        vendorMenu.setItem(inv, exitMenuButton, vendorMenu.getExitMenu());
    }

    private void fetchSections() {
        pageSectionData.clear();
        VendorEntity vendor = vendorMenu.getVendorService().getVendor(vendorMenu.getVendorName()); 
        if(vendor == null) {
            return;
        }
        Set<VendorSectionEntity> sections = vendor.getSections();
        if(sections.isEmpty()) {
            return;
        }
        Queue<VendorSectionEntity> temp = new ArrayDeque<>();
        temp.addAll(sections);
        int maxPages = (int) Math.ceil((double) sections.size() / (double) sectionsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, VendorSectionEntity> options = new LinkedHashMap<>();
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
        Map<List<Integer>, VendorSectionEntity> sections = pageSectionData.getOrDefault(sectionPageNumbers.get(player)[0], Collections.emptyMap());
        Set<VendorItemEntity> items = Collections.emptySet();
        if(sections.containsKey(Arrays.asList(sectionCoords[0], sectionCoords[1]))) {
            items = sections.get(Arrays.asList(sectionCoords[0], sectionCoords[1])).getItems();
        }
        if(sections.isEmpty() || items.isEmpty()) {
            return;
        }
        Queue<VendorItemEntity> temp = new ArrayDeque<>();
        temp.addAll(items);
        int maxPages = (int) Math.ceil((double) items.size() / (double) itemsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, VendorItemEntity> options = new LinkedHashMap<>();
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
        Map<List<Integer>, VendorSectionEntity> sections = pageSectionData.getOrDefault(sectionPageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, VendorSectionEntity> entry : sections.entrySet()) {
            List<Integer> coords = entry.getKey();
            VendorSectionEntity section = entry.getValue();
            if(section == null) {
                vendorMenu.setItem(inv, coords, vendorMenu.getUnavailable());
            } else {
                ItemStack item = section.getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    Component description = Component.text("Section", NamedTextColor.GRAY);
                    meta.lore(Arrays.asList(description));
                    item.setItemMeta(meta);
                }
                vendorMenu.setItem(inv, coords, item);
            }
        }
    }

    public void displayInitialItems(Inventory inv, Player player, int[] sectionCoords) {
        vendorMenu.rectangleAreaLoading(inv, itemsStart, itemsWidth, itemsLength);
        Scheduler.runAsync((task) -> {
            fetchItems(inv, player, sectionCoords);
            Map<List<Integer>, VendorItemEntity> items = pageItemData.getOrDefault(itemPageNumbers.get(player)[0], Collections.emptyMap());
            for(Map.Entry<List<Integer>, VendorItemEntity> entry : items.entrySet()) {
                List<Integer> coords = entry.getKey();
                VendorItemEntity item = entry.getValue();
                if(item == null) {
                    vendorMenu.setItem(inv, coords, vendorMenu.getUnavailable());
                } else {
                    ItemStack icon = item.getIcon().clone();
                    vendorMenu.setItem(inv, coords, icon);
                }
            }
        });
    }

    public void displayItems(Inventory inv, Player player) {
        Map<List<Integer>, VendorItemEntity> items = pageItemData.getOrDefault(itemPageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, VendorItemEntity> entry : items.entrySet()) {
            List<Integer> coords = entry.getKey();
            VendorItemEntity item = entry.getValue();
            if(item == null) {
                vendorMenu.setItem(inv, coords, vendorMenu.getUnavailable());
            } else {
                ItemStack icon = item.getIcon().clone();
                vendorMenu.setItem(inv, coords, icon);
            }
        }
    }
}
