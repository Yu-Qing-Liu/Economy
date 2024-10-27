package com.github.yuqingliu.economy.view.pursemenu.mainmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController {
    private final PurseMenu purseMenu;
    private final int[] prevPageButton = new int[]{1,1};
    private final int[] nextPageButton = new int[]{7,1};
    private final int[] currencyStart = new int[]{2,1};
    private final int currencyLength = 5;
    private final int currencyWidth = 1;
    private final int currencySize = currencyLength * currencyWidth;
    private final List<int[]> currencies;
    private Map<Integer, Map<List<Integer>, CurrencyEntity>> pageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> pageNumbers = new ConcurrentHashMap<>();

    public MainMenuController(PurseMenu purseMenu) {
        this.purseMenu = purseMenu;
        this.currencies = purseMenu.rectangleArea(currencyStart, currencyWidth, currencyLength);
    }
    
    public void openMainMenu(Player player, Inventory inv) {
        pageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            purseMenu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        purseMenu.fill(inv, purseMenu.getBackgroundItems().get(Material.BROWN_STAINED_GLASS_PANE));
        buttons(inv);
        purseMenu.rectangleAreaLoading(inv, currencyStart, currencyWidth, currencyLength);
        Scheduler.runAsync((task) -> {
            fetchCurrencies(player);
            displayCurrencies(player, inv);
        });
    }

    public void nextPage(Player player, Inventory inv) {
        pageNumbers.get(player)[0]++;
        if(pageData.containsKey(pageNumbers.get(player)[0])) {
            displayCurrencies(player, inv); 
        } else {
            pageNumbers.get(player)[0]--;
        }     
    }

    public void prevPage(Player player, Inventory inv) {
        pageNumbers.get(player)[0]--;
        if(pageNumbers.get(player)[0] > 0) {
            displayCurrencies(player, inv);
        } else {
            pageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        pageNumbers.remove(player);
    }

    private void fetchCurrencies(Player player) {
        pageData.clear();
        Set<CurrencyEntity> currencies = purseMenu.getCurrencyService().getPlayerPurseCurrencies(player);
        if(currencies.isEmpty()) {
            return;
        }
        Queue<CurrencyEntity> temp = new ArrayDeque<>();
        temp.addAll(currencies);
        int maxPages = (int) Math.ceil((double) currencies.size() / (double) currencySize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, CurrencyEntity> options = new LinkedHashMap<>();
            for (int[] coords : this.currencies) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayCurrencies(Player player, Inventory inv) {
        Map<List<Integer>, CurrencyEntity> options = pageData.getOrDefault(pageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, CurrencyEntity> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            CurrencyEntity currency = entry.getValue();
            if(currency == null) {
                purseMenu.setItem(inv, coords, purseMenu.getUnavailable());
            } else {
                double amount = currency.getAmount();
                ItemStack item = currency.getIcon().clone();
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    Component balance = Component.text("Balance ", NamedTextColor.GRAY).append(Component.text(amount + "$", NamedTextColor.GREEN));
                    List<Component> lore = new ArrayList<>();
                    lore.add(balance);
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                purseMenu.setItem(inv, coords, item);
            }
        }
    }

    private void buttons(Inventory inv) {
        purseMenu.setItem(inv, nextPageButton, purseMenu.getNextPage());
        purseMenu.setItem(inv, prevPageButton, purseMenu.getPrevPage());
    }
}
