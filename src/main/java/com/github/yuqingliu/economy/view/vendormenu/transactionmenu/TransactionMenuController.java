package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class TransactionMenuController extends VendorMenu {
    protected final int prevPagePtr = 16;
    protected final int nextPagePtr = 43;
    protected final int prev = 25;
    protected final int exit = 34;
    protected final int length = 24;
    protected Material voidOption = Material.GLASS_PANE;
    protected final List<Integer> options = Arrays.asList(10,11,12,13,14,15,19,20,21,22,23,24,28,29,30,31,32,33,37,38,39,40,41,42);
    protected final List<Integer> buttons = Arrays.asList(16,43,25,34);
    protected Map<Integer, CurrencyOption[]> pageData = new HashMap<>();
    protected int pageNumber = 1;
    protected VendorItemEntity item;
    
    public TransactionMenuController(EventManager eventManager, Component displayName, VendorService vendorService, CurrencyService currencyService) {
        super(eventManager, displayName, vendorService, currencyService);
    }

    public void openTransactionMenu(Inventory inv, VendorItemEntity item) {
        this.item = item;
        clear(inv);
        pagePtrs(inv);
        frame(inv);
        fetchOptions();
        displayOptions(inv);
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
            ItemStack icon = currencyService.getCurrencyByName(currencyName).getIcon();
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
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if(!options.contains(i) && !buttons.contains(i)) {
                inv.setItem(i, Placeholder);
            }
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
