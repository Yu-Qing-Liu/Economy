package com.github.yuqingliu.economy.view.pursemenu.mainmenu;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController extends PurseMenu {
    private final int length = 7;
    private final int prevPagePtr = 9;
    private final int nextPagePtr = 17;
    private Material voidOption = Material.GLASS_PANE;
    private final List<Integer> options = Arrays.asList(10,11,12,13,14,15,16);
    private final List<Integer> buttons = Arrays.asList(9,17);

    private Map<Integer, CurrencyEntity[]> pageData = new HashMap<>();
    private int pageNumber = 1;

    public MainMenuController(EventManager eventManager, Component displayName, CurrencyService currencyService) {
        super(eventManager, displayName, currencyService);
    }
    
    public void openMainMenu(Player player, Inventory inv) {
        currentMenu = MenuType.MainMenu;
        clear(inv);
        pagePtrs(inv);
        frame(inv);
        fetchCurrencies(player);
        displayCurrencies(player, inv);
    }

    public void nextPage(Player player, Inventory inv) {
        pageNumber++;
        if(pageData.containsKey(pageNumber)) {
            displayCurrencies(player, inv); 
        } else {
            pageNumber--;
        }     
    }

    public void prevPage(Player player, Inventory inv) {
        pageNumber--;
        if(pageNumber > 0) {
            displayCurrencies(player, inv);
        } else {
            pageNumber++;
        }
    }

    public void onClose() {
        pageData.clear();
    }

    private void fetchCurrencies(Player player) {
        Set<CurrencyEntity> currencies = currencyService.getPlayerPurseCurrencies(player);
        if(currencies == null || currencies.isEmpty()) {
            return;
        }
        Queue<CurrencyEntity> temp = new ArrayDeque<>();
        temp.addAll(currencies);
        int maxPages = (int) Math.ceil((double) currencies.size() / (double) length);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            CurrencyEntity[] options = new CurrencyEntity[length];
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

    private void displayCurrencies(Player player, Inventory inv) {
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        CurrencyEntity[] options = pageData.getOrDefault(pageNumber, new CurrencyEntity[length]);
        int currentIndex = 0;
        for (int i : this.options) {
            if(options[currentIndex] == null) {
                inv.setItem(i, Placeholder);
            } else {
                double amount = options[currentIndex].getAmount();
                ItemStack item = options[currentIndex].getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    Component balance = Component.text("Balance ", NamedTextColor.GRAY).append(Component.text(amount + "$", NamedTextColor.GREEN));
                    List<Component> lore = new ArrayList<>();
                    lore.add(balance);
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(i, item);
            }
            currentIndex++;
        }
    }

    private void frame(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
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
    }
}
