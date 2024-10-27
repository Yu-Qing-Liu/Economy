package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class OrderMenuController {
    private final ShopMenu shopMenu;
    private final int[] prevMenuButton = new int[]{1,1};
    private final int[] exitMenuButton = new int[]{2,1};
    private final int[] refreshButton = new int[]{7,1};
    private final int[] prevBuyOrdersButton = new int[]{1,3};
    private final int[] prevSellOrdersButton = new int[]{1,4};
    private final int[] nextBuyOrdersButton = new int[]{7,3};
    private final int[] nextSellOrdersButton = new int[]{7,4};
    private final int[] createBuyOrderButton = new int[]{2,3};
    private final int[] createSellOrderButton = new int[]{2,4};
    private final int[] itemSlot = new int[]{4,1};
    private final int[] buyOrdersStart = new int[]{3,3};
    private final int[] sellOrdersStart = new int[]{3,4};
    private final int buyOrdersWidth = 1;
    private final int buyOrdersLength = 4;
    private final int sellOrdersWidth = 1;
    private final int sellOrdersLength = 4;
    private final int buyOrdersSize = buyOrdersWidth * buyOrdersLength;
    private final int sellOrdersSize = sellOrdersWidth * sellOrdersLength;
    private final List<int[]> buyOrders;
    private final List<int[]> sellOrders;
    private Map<Integer, Map<List<Integer>, OrderOption>> buyPageData = new ConcurrentHashMap<>();
    private Map<Integer, Map<List<Integer>, OrderOption>> sellPageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> buyPageNumbers = new ConcurrentHashMap<>();
    private Map<Player, int[]> sellPageNumbers = new ConcurrentHashMap<>();
    private ShopItemEntity item;
    
    public OrderMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.buyOrders = shopMenu.rectangleArea(buyOrdersStart, buyOrdersWidth, buyOrdersLength);
        this.sellOrders = shopMenu.rectangleArea(sellOrdersStart, sellOrdersWidth, sellOrdersLength);
    }

    public void openOrderMenu(Inventory inv, ShopItemEntity item, Player player) {
        this.item = item;
        buyPageNumbers.put(player, new int[]{1});
        sellPageNumbers.put(player, new int[]{1});
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.OrderMenu);
        }, Duration.ofMillis(50));
        shopMenu.fill(inv, shopMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        border(inv);
        buttons(inv);
        displayItem(inv);
        shopMenu.rectangleAreaLoading(inv, buyOrdersStart, buyOrdersWidth, buyOrdersLength);
        shopMenu.rectangleAreaLoading(inv, sellOrdersStart, sellOrdersWidth, sellOrdersLength);
        Scheduler.runAsync((task) -> {
            reload(inv, player);
        });
    }

    public void reload(Inventory inv, Player player) {
        fetchBuyOptions();
        fetchSellOptions();
        displayBuyOptions(inv, player);
        displaySellOptions(inv, player);
        buttons(inv);
    }

    public void nextBuyPage(Inventory inv, Player player) {
        buyPageNumbers.get(player)[0]++;
        if(buyPageData.containsKey(buyPageNumbers.get(player)[0])) {
            displayBuyOptions(inv, player);
        } else {
            buyPageNumbers.get(player)[0]--;
        }     
    }

    public void prevBuyPage(Inventory inv, Player player) {
        buyPageNumbers.get(player)[0]--;
        if(buyPageNumbers.get(player)[0] > 0) {
            displayBuyOptions(inv, player);
        } else {
            buyPageNumbers.get(player)[0]++;
        }
    }

    public void nextSellPage(Inventory inv, Player player) {
        sellPageNumbers.get(player)[0]++;
            if(sellPageData.containsKey(sellPageNumbers.get(player)[0])) {
                displayBuyOptions(inv, player);
            } else {
                sellPageNumbers.get(player)[0]--;
            }     
        }

    public void prevSellPage(Inventory inv, Player player) {
        sellPageNumbers.get(player)[0]--;
        if(sellPageNumbers.get(player)[0] > 0) {
            displayBuyOptions(inv, player);
        } else {
            sellPageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        buyPageNumbers.remove(player);
        sellPageNumbers.remove(player);
    }

    private void border(Inventory inv) {
        ItemStack borderItem = shopMenu.createSlotItem(Material.BLACK_STAINED_GLASS_PANE, shopMenu.getUnavailableComponent());
        shopMenu.fillRectangleArea(inv, new int[]{1,2}, 1, 7, borderItem);
        shopMenu.fillRectangleArea(inv, new int[]{3,0}, 2, 3, borderItem);
    }

    private void displayItem(Inventory inv) {
        shopMenu.setItem(inv, itemSlot, item.getIcon().clone());
    }

    private void buttons(Inventory inv) {
        shopMenu.setItem(inv, prevBuyOrdersButton, shopMenu.getPrevPage());
        shopMenu.setItem(inv, nextBuyOrdersButton, shopMenu.getNextPage());
        shopMenu.setItem(inv, prevSellOrdersButton, shopMenu.getPrevPage());
        shopMenu.setItem(inv, nextSellOrdersButton, shopMenu.getNextPage());
        shopMenu.setItem(inv, prevMenuButton, shopMenu.getPrevMenu());
        shopMenu.setItem(inv, exitMenuButton, shopMenu.getExitMenu());
        shopMenu.setItem(inv, refreshButton, shopMenu.createSlotItem(Material.YELLOW_WOOL, Component.text("Refresh", NamedTextColor.YELLOW)));
        shopMenu.setItem(inv, createBuyOrderButton, shopMenu.createSlotItem(Material.WRITABLE_BOOK, Component.text("Create Buy Order", NamedTextColor.LIGHT_PURPLE)));
        shopMenu.setItem(inv, createSellOrderButton, shopMenu.createSlotItem(Material.WRITABLE_BOOK, Component.text("Create Sell Order", NamedTextColor.LIGHT_PURPLE)));
    }

    private void fetchBuyOptions() {
        buyPageData.clear();
        Map<String, Set<ShopOrderEntity>> buyOrders = item.getBuyOrders();
        Queue<OrderOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, Set<ShopOrderEntity>> entry : buyOrders.entrySet()) {
            String currencyName = entry.getKey();
            Set<ShopOrderEntity> orders = buyOrders.get(currencyName);
            ItemStack icon =  shopMenu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            OrderOption option = new OrderOption(icon, orders);
            temp.offer(option);
        }
        int maxPages = (int) Math.ceil((double) temp.size() / (double) buyOrdersSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, OrderOption> options = new HashMap<>();
            for (int[] coords : this.buyOrders) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            buyPageData.put(pageNum, options);
        }
    }

    private void fetchSellOptions() {
        sellPageData.clear();
        Map<String, Set<ShopOrderEntity>> sellOrders = item.getSellOrders();
        Queue<OrderOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, Set<ShopOrderEntity>> entry : sellOrders.entrySet()) {
            String currencyName = entry.getKey();
            Set<ShopOrderEntity> orders = sellOrders.get(currencyName);
            ItemStack icon =  shopMenu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            OrderOption option = new OrderOption(icon, orders);
            temp.offer(option);
        }
        int maxPages = (int) Math.ceil((double) temp.size() / (double) sellOrdersSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, OrderOption> options = new HashMap<>();
            for (int[] coords : this.sellOrders) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            sellPageData.put(pageNum, options);
        }
    }


    private void displayBuyOptions(Inventory inv, Player player) {
        Map<List<Integer>, OrderOption> currencyOptions = buyPageData.getOrDefault(buyPageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, OrderOption> entry : currencyOptions.entrySet()) {
            List<Integer> coords = entry.getKey();
            OrderOption option = entry.getValue();
            if(option == null) {
                shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
            } else {
                ItemStack item = option.getIcon().clone(); 
                List<Component> topOrders = new ArrayList<>();
                Set<ShopOrderEntity> orders = option.getOrders();
                int count = 0;
                int valid = 0;
                int maxCount = 3;
                Iterator<ShopOrderEntity> iterator = orders.iterator();
                while (iterator.hasNext() && count < maxCount) {
                    ShopOrderEntity element = iterator.next();
                    if(element.getQuantity() - element.getFilledQuantity() > 0) {
                        Component playerComponent = Component.text(Bukkit.getOfflinePlayer(element.getPlayerId()).getName(), NamedTextColor.LIGHT_PURPLE);
                        Component priceComponent = Component.text("Unit Buy Price: ", NamedTextColor.BLUE).append(Component.text(element.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
                        Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(element.getQuantity() - element.getFilledQuantity() + "x", NamedTextColor.GREEN));
                        Component separator = Component.text("------------------------------------", NamedTextColor.BLUE);
                        topOrders.add(playerComponent);
                        topOrders.add(priceComponent);
                        topOrders.add(quantityComponent);
                        topOrders.add(separator);
                        valid++;
                    }
                    count++;
                }
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    meta.lore(topOrders);
                }
                item.setItemMeta(meta);
                if(valid > 0) {
                    shopMenu.setItem(inv, coords, item);
                } else {
                    shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
                }
            }
        }
    }

    private void displaySellOptions(Inventory inv, Player player) {
        Map<List<Integer>, OrderOption> currencyOptions = sellPageData.getOrDefault(sellPageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, OrderOption> entry : currencyOptions.entrySet()) {
            List<Integer> coords = entry.getKey();
            OrderOption option = entry.getValue();
            if(option == null) {
                shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
            } else {
                ItemStack item = option.getIcon().clone(); 
                List<Component> topOrders = new ArrayList<>();
                Set<ShopOrderEntity> orders = option.getOrders();
                int count = 0;
                int valid = 0;
                int maxCount = 3;
                Iterator<ShopOrderEntity> iterator = orders.iterator();
                while (iterator.hasNext() && count < maxCount) {
                    ShopOrderEntity element = iterator.next();
                    if(element.getQuantity() - element.getFilledQuantity() > 0) {
                        Component playerComponent = Component.text(Bukkit.getOfflinePlayer(element.getPlayerId()).getName(), NamedTextColor.LIGHT_PURPLE);
                        Component priceComponent = Component.text("Unit Sell Price: ", NamedTextColor.BLUE).append(Component.text(element.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
                        Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(element.getQuantity() - element.getFilledQuantity() + "x", NamedTextColor.GREEN));
                        Component separator = Component.text("------------------------------------", NamedTextColor.BLUE);
                        topOrders.add(playerComponent);
                        topOrders.add(priceComponent);
                        topOrders.add(quantityComponent);
                        topOrders.add(separator);
                        valid++;
                    }
                    count++;
                }
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    meta.lore(topOrders);
                }
                item.setItemMeta(meta);
                if(valid > 0) {
                    shopMenu.setItem(inv, coords, item);
                } else {
                    shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
                }
            }
        }
    }
}
