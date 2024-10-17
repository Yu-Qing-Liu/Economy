package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
    protected final int prevPagePtr = 28;
    protected final int nextPagePtr = 34;
    protected final int prev = 11;
    protected final int exit = 15;
    protected final int itemSlot = 13;
    protected final int length = 5;
    protected Material voidOption = Material.GLASS_PANE;
    protected final List<Integer> options = Arrays.asList(29,30,31,32,33);
    protected final List<Integer> buttons = Arrays.asList(11,13,15,28,34);
    protected final List<Integer> border = Arrays.asList(3,4,5,12,14,19,20,21,22,23,24,25,37,38,39,40,41,42,43);
    protected Map<Integer, CurrencyOption[]> pageData = new HashMap<>();
    protected int pageNumber = 1;
    protected VendorItemEntity item;
    
    public TransactionMenuController(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
    }

    public void openTransactionMenu(Inventory inv, VendorItemEntity item, Player player) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            vendorMenu.getPlayerMenuTypes().put(player, MenuType.TransactionMenu);
        }, Duration.ofMillis(50));
        vendorMenu.clear(inv);
        frame(inv);
        pagePtrs(inv);
        border(inv);
        displayItem(inv);
        Scheduler.runAsync((task) -> {
            fetchOptions();
            displayOptions(inv);
        });
    }

    public void nextPage(Inventory inv) {
        pageNumber++;
        if(pageData.containsKey(pageNumber)) {
            displayOptions(inv);
        } else {
            pageNumber--;
        }     
    }

    public void prevPage(Inventory inv) {
        pageNumber--;
        if(pageNumber > 0) {
            displayOptions(inv);
        } else {
            pageNumber++;
        }
    }

    public void onClose() {
        pageData.clear();
    }

    private void fetchOptions() {
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

        int maxPages = (int) Math.ceil((double) temp.size() / (double) length);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            CurrencyOption[] options = new CurrencyOption[length];
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

    private void displayItem(Inventory inv) {
        inv.setItem(itemSlot, item.getIcon().clone());
    }

    private void displayOptions(Inventory inv) {
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        CurrencyOption[] currencyOptions = pageData.getOrDefault(pageNumber, new CurrencyOption[length]);
        int currentIndex = 0;
        for(int i : options) {
            if(currencyOptions[currentIndex] == null) {
                inv.setItem(i, Placeholder);
            } else {
                ItemStack item = currencyOptions[currentIndex].getIcon().clone(); 
                ItemMeta itemMeta = item.getItemMeta();
                Component buyPrice = Component.text("UNIT BUY PRICE: ", NamedTextColor.DARK_AQUA).append(Component.text(currencyOptions[currentIndex].getBuyPrice() +"$ ", NamedTextColor.DARK_GREEN));
                Component sellPrice = Component.text("UNIT SELL PRICE: ", NamedTextColor.DARK_AQUA).append(Component.text(currencyOptions[currentIndex].getSellPrice() +"$ ", NamedTextColor.DARK_GREEN));
                itemMeta.lore(Arrays.asList(buyPrice, sellPrice));
                item.setItemMeta(itemMeta);
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
        for (int i = 0; i < vendorMenu.getInventorySize(); i++) {
            inv.setItem(i, Placeholder);
        }
    }

    private void border(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = Placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(meta);
        for (int i : border) {
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
            prevmeta.displayName(Component.text("Items", NamedTextColor.GRAY));
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
