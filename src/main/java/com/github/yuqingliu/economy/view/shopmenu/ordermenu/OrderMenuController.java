package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

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
    protected final int prevBuyPagePtr = 28;
    protected final int nextBuyPagePtr = 34;
    protected final int prevSellPagePtr = 37;
    protected final int nextSellPagePtr = 43;
    protected final int prev = 11;
    protected final int exit = 15;
    protected final int createBuyOrder = 29;
    protected final int createSellOrder = 38;
    protected final int itemSlot = 13;
    protected final int length = 4;
    protected Material voidOption = Material.GLASS_PANE;
    protected final List<Integer> buyOptions = Arrays.asList(30,31,32,33);
    protected final List<Integer> sellOptions = Arrays.asList(39,40,41,42);
    protected final List<Integer> buttons = Arrays.asList(10,11,13,15,16,28,34,37,43);
    protected final List<Integer> border = Arrays.asList(3,4,5,12,14,19,20,21,22,23,24,25);
    protected Map<Integer, OrderOption[]> buyPageData = new HashMap<>();
    protected Map<Integer, OrderOption[]> sellPageData = new HashMap<>();
    protected int buyPageNumber = 1;
    protected int sellPageNumber = 1;
    protected ShopItemEntity item;
    protected BukkitTask renderTask;
    
    public OrderMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }

    public void openOrderMenu(Inventory inv, ShopItemEntity item, Player player) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.OrderMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
        frame(inv);
        pagePtrs(inv);
        border(inv);
        displayItem(inv);
        renderTask = Scheduler.runAsync((task) -> {
            fetchBuyOptions();
            fetchSellOptions();
            displayBuyOptions(inv);
            displaySellOptions(inv);
            buttons(inv);
        });
    }

    public void nextBuyPage(Inventory inv) {
        buyPageNumber++;
        if(buyPageData.containsKey(buyPageNumber)) {
            displayBuyOptions(inv);
        } else {
            buyPageNumber--;
        }     
    }

    public void prevBuyPage(Inventory inv) {
        buyPageNumber--;
        if(buyPageNumber > 0) {
            displayBuyOptions(inv);
        } else {
            buyPageNumber++;
        }
    }

    public void nextSellPage(Inventory inv) {
            buyPageNumber++;
            if(buyPageData.containsKey(buyPageNumber)) {
                displayBuyOptions(inv);
            } else {
                buyPageNumber--;
            }     
        }

    public void prevSellPage(Inventory inv) {
        buyPageNumber--;
        if(buyPageNumber > 0) {
            displayBuyOptions(inv);
        } else {
            buyPageNumber++;
        }
    }

    public void onClose() {
        buyPageData.clear();
        sellPageData.clear();
    }

    private void fetchBuyOptions() {
        Map<String, Set<ShopOrderEntity>> buyOrders = item.getBuyOrders();
        Queue<OrderOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, Set<ShopOrderEntity>> entry : buyOrders.entrySet()) {
            String currencyName = entry.getKey();
            Set<ShopOrderEntity> orders = buyOrders.get(currencyName);
            ItemStack icon =  shopMenu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            OrderOption option = new OrderOption(icon, orders);
            temp.offer(option);
        }

        int maxPages = (int) Math.ceil((double) temp.size() / (double) length);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            OrderOption[] options = new OrderOption[length];
            for (int j = 0; j < length; j++) {
                if(temp.isEmpty()) {
                    options[j] = null;
                } else {
                    options[j] = temp.poll();
                }
            }
            buyPageData.put(pageNum, options);
        }
    }

    private void fetchSellOptions() {
        Map<String, Set<ShopOrderEntity>> sellOrders = item.getSellOrders();
        Queue<OrderOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, Set<ShopOrderEntity>> entry : sellOrders.entrySet()) {
            String currencyName = entry.getKey();
            Set<ShopOrderEntity> orders = sellOrders.get(currencyName);
            ItemStack icon =  shopMenu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            OrderOption option = new OrderOption(icon, orders);
            temp.offer(option);
        }

        int maxPages = (int) Math.ceil((double) temp.size() / (double) length);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            OrderOption[] options = new OrderOption[length];
            for (int j = 0; j < length; j++) {
                if(temp.isEmpty()) {
                    options[j] = null;
                } else {
                    options[j] = temp.poll();
                }
            }
            sellPageData.put(pageNum, options);
        }
    }

    private void displayItem(Inventory inv) {
        inv.setItem(itemSlot, item.getIcon().clone());
    }

    private void displayBuyOptions(Inventory inv) {
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        OrderOption[] currencyOptions = buyPageData.getOrDefault(buyPageNumber, new OrderOption[length]);
        int currentIndex = 0;
        for(int i : buyOptions) {
            OrderOption opt = currencyOptions[currentIndex];
            if(opt == null) {
                inv.setItem(i, Placeholder);
            } else {
                ItemStack item = opt.getIcon().clone(); 
                List<Component> topOrders = new ArrayList<>();
                Set<ShopOrderEntity> orders = opt.getOrders();
                int count = 0;
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
                    }
                    count++;
                }
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    meta.lore(topOrders);
                }
                item.setItemMeta(meta);
                inv.setItem(i, item);
            }
            currentIndex++;
        }
    }

    private void displaySellOptions(Inventory inv) {
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        OrderOption[] currencyOptions = sellPageData.getOrDefault(sellPageNumber, new OrderOption[length]);
        int currentIndex = 0;
        for(int i : sellOptions) {
            OrderOption opt = currencyOptions[currentIndex];
            if(opt == null) {
                inv.setItem(i, Placeholder);
            } else {
                ItemStack item = opt.getIcon().clone(); 
                List<Component> topOrders = new ArrayList<>();
                Set<ShopOrderEntity> orders = opt.getOrders();
                int count = 0;
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
                    }
                    count++;
                }
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    meta.lore(topOrders);
                }
                item.setItemMeta(meta);
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
        for (int i = 0; i < shopMenu.getInventorySize(); i++) {
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

    private void buttons(Inventory inv) {
        ItemStack buyOrder = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta buyMeta = buyOrder.getItemMeta();
        if(buyMeta != null) {
            buyMeta.displayName(Component.text("Create Buy Order", NamedTextColor.LIGHT_PURPLE));
        }
        buyOrder.setItemMeta(buyMeta);
        inv.setItem(this.createBuyOrder, buyOrder);

        ItemStack sellOrder = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta sellMeta = sellOrder.getItemMeta();
        if(sellMeta != null) {
            sellMeta.displayName(Component.text("Create Sell Order", NamedTextColor.LIGHT_PURPLE));
        }
        sellOrder.setItemMeta(sellMeta);
        inv.setItem(this.createSellOrder, sellOrder);
    }

    private void pagePtrs(Inventory inv) {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nmeta = nextPage.getItemMeta();
        if(nmeta != null) {
            nmeta.displayName(Component.text("Next Page", NamedTextColor.AQUA));
        }
        nextPage.setItemMeta(nmeta);
        inv.setItem(nextBuyPagePtr, nextPage);
        inv.setItem(nextSellPagePtr, nextPage);

        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta pmeta = prevPage.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Previous Page", NamedTextColor.AQUA));
        }
        prevPage.setItemMeta(pmeta);
        inv.setItem(prevBuyPagePtr, prevPage);
        inv.setItem(prevSellPagePtr, prevPage);

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
