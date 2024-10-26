package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class TransactionMenuController {
    private final VendorMenu vendorMenu;
    private final int[] prevOptionsButton = new int[]{1,3};
    private final int[] nextOptionsButton = new int[]{1,4};
    private final int[] prevMenuButton = new int[]{7,3};
    private final int[] exitMenuButton = new int[]{7,4};
    private final int[] itemSlot = new int[]{4,1};
    private final int optionsLength = 5;
    private final int optionsWidth = 2;
    private final int optionsSize = optionsLength * optionsWidth;
    private final int[] optionsStart = new int[]{2,3};
    private final List<int[]> currencyOptions;
    private Map<Integer, Map<List<Integer>, CurrencyOption>> pageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> pageNumbers = new ConcurrentHashMap<>();
    private VendorItemEntity item;
    
    public TransactionMenuController(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.currencyOptions = vendorMenu.rectangleArea(optionsStart, optionsWidth, optionsLength);
    }

    public void openTransactionMenu(Inventory inv, VendorItemEntity item, Player player) {
        this.item = item;
        pageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            vendorMenu.getPlayerMenuTypes().put(player, MenuType.TransactionMenu);
        }, Duration.ofMillis(50));
        vendorMenu.clear(inv);
        vendorMenu.fill(inv, vendorMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        border(inv);
        buttons(inv);
        displayItem(inv);
        vendorMenu.rectangleAreaLoading(inv, optionsStart, optionsWidth, optionsLength);
        Scheduler.runAsync((task) -> {
            fetchOptions();
            displayOptions(inv, player);
        });
    }

    public void nextPage(Inventory inv, Player player) {
        pageNumbers.get(player)[0]++;
        if(pageData.containsKey(pageNumbers.get(player)[0])) {
            displayOptions(inv, player);
        } else {
            pageNumbers.get(player)[0]--;
        }     
    }

    public void prevPage(Inventory inv, Player player) {
        pageNumbers.get(player)[0]--;
        if(pageNumbers.get(player)[0] > 0) {
            displayOptions(inv, player);
        } else {
            pageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        pageNumbers.remove(player);
    }

    private void displayItem(Inventory inv) {
        vendorMenu.setItem(inv, itemSlot, item.getIcon().clone());
    }

    private void border(Inventory inv) {
        ItemStack borderItem = vendorMenu.createSlotItem(Material.BLACK_STAINED_GLASS_PANE, vendorMenu.getUnavailableComponent());
        vendorMenu.fillRectangleArea(inv, new int[]{1,0}, 1, 7, borderItem);
        vendorMenu.fillRectangleArea(inv, new int[]{1,1}, 1, 3, borderItem);
        vendorMenu.fillRectangleArea(inv, new int[]{5,1}, 1, 3, borderItem);
        vendorMenu.fillRectangleArea(inv, new int[]{1,2}, 1, 7, borderItem);
        vendorMenu.fillRectangleArea(inv, new int[]{1,5}, 1, 7, borderItem);
    }

    private void buttons(Inventory inv) {
        vendorMenu.setItem(inv, prevOptionsButton, vendorMenu.getPrevPage());
        vendorMenu.setItem(inv, nextOptionsButton, vendorMenu.getNextPage());
        vendorMenu.setItem(inv, prevMenuButton, vendorMenu.getPrevMenu());
        vendorMenu.setItem(inv, exitMenuButton, vendorMenu.getExitMenu());
    }

    private void fetchOptions() {
        pageData.clear();
        Map<String, Double> buyPrices = item.getBuyPrices();
        Map<String, Double> sellPrices = item.getSellPrices();
        Queue<CurrencyOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, Double> entry : buyPrices.entrySet()) {
            String currencyName = entry.getKey();
            double buyPrice = entry.getValue();
            double sellPrice = sellPrices.get(currencyName);
            ItemStack icon =  vendorMenu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            CurrencyOption option = new CurrencyOption(icon, buyPrice, sellPrice);
            temp.offer(option);
        }
        int maxPages = (int) Math.ceil((double) temp.size() / (double) optionsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, CurrencyOption> options = new HashMap<>();
            for (int[] coords : currencyOptions) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayOptions(Inventory inv, Player player) {
        Map<List<Integer>, CurrencyOption> options = pageData.getOrDefault(pageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, CurrencyOption> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            CurrencyOption option = entry.getValue();
            if(option == null) {
                vendorMenu.setItem(inv, coords, vendorMenu.getUnavailable());
            } else {
                ItemStack item = option.getIcon().clone();
                ItemMeta itemMeta = item.getItemMeta();
                Component buyPrice = Component.text("UNIT BUY PRICE: ", NamedTextColor.DARK_AQUA).append(Component.text(option.getBuyPrice() +"$ ", NamedTextColor.DARK_GREEN));
                Component sellPrice = Component.text("UNIT SELL PRICE: ", NamedTextColor.DARK_AQUA).append(Component.text(option.getSellPrice() +"$ ", NamedTextColor.DARK_GREEN));
                itemMeta.lore(Arrays.asList(buyPrice, sellPrice));
                item.setItemMeta(itemMeta);
                vendorMenu.setItem(inv, coords, item);
            }
        }
    }
}
