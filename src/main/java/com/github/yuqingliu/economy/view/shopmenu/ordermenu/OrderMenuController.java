package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.PageData;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class OrderMenuController extends AbstractPlayerInventoryController<ShopMenu> {
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
    private final PageData<OrderOption> buyPageData = new PageData<>();
    private final PageData<OrderOption> sellPageData = new PageData<>();
    private ShopItemEntity item;
    
    public OrderMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
        this.buyOrders = rectangleArea(buyOrdersStart, buyOrdersWidth, buyOrdersLength);
        this.sellOrders = rectangleArea(sellOrdersStart, sellOrdersWidth, sellOrdersLength);
    }

    public void openMenu(ShopItemEntity item) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.OrderMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayItem();
        rectangleAreaLoading(buyOrdersStart, buyOrdersWidth, buyOrdersLength);
        rectangleAreaLoading(sellOrdersStart, sellOrdersWidth, sellOrdersLength);
        reload();
    }

    public void reload() {
        if (item == null) {
            return;
        }
        item = menu.getShopService().getShopItem(item.getShopName(),item.getShopSection().getSectionName(), item.getItemName());
        Scheduler.runAsync(t -> {
            fetchBuyOptions();
            fetchSellOptions();
            displayBuyOptions();
            displaySellOptions();
            buttons();
        });
    }

    public void nextBuyPage() {
        buyPageData.nextPage(() -> displayBuyOptions());
    }

    public void prevBuyPage() {
        buyPageData.prevPage(() -> displayBuyOptions());
    }

    public void nextSellPage() {
        sellPageData.nextPage(() -> displaySellOptions());
    }

    public void prevSellPage() {
        sellPageData.prevPage(() -> displaySellOptions());
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{1,2}, 1, 7, borderItem);
        fillRectangleArea(new int[]{3,0}, 2, 3, borderItem);
    }

    private void displayItem() {
        setItem(itemSlot, item.getIcon().clone());
    }

    private void buttons() {
        setItem(prevBuyOrdersButton, getPrevPageIcon());
        setItem(nextBuyOrdersButton, getNextPageIcon());
        setItem(prevSellOrdersButton, getPrevPageIcon());
        setItem(nextSellOrdersButton, getNextPageIcon());
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(refreshButton, getReloadIcon());
        setItem(createBuyOrderButton, createSlotItem(Material.WRITABLE_BOOK, Component.text("Create Buy Order", NamedTextColor.LIGHT_PURPLE)));
        setItem(createSellOrderButton, createSlotItem(Material.WRITABLE_BOOK, Component.text("Create Sell Order", NamedTextColor.LIGHT_PURPLE)));
    }

    private void fetchBuyOptions() {
        if (item == null) {
            return;
        }
        buyPageData.clear();
        Map<String, List<ShopOrderEntity>> buyOrders = item.getBuyOrders();
        Queue<OrderOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, List<ShopOrderEntity>> entry : buyOrders.entrySet()) {
            String currencyName = entry.getKey();
            List<ShopOrderEntity> orders = buyOrders.get(currencyName);
            ItemStack icon =  menu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            OrderOption option = new OrderOption(icon, orders);
            temp.offer(option);
        }
        int maxPages = (int) Math.ceil((double) temp.size() / (double) buyOrdersSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, OrderOption> options = new LinkedHashMap<>();
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
        if (item == null) {
            return;
        }
        sellPageData.clear();
        Map<String, List<ShopOrderEntity>> sellOrders = item.getSellOrders();
        Queue<OrderOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, List<ShopOrderEntity>> entry : sellOrders.entrySet()) {
            String currencyName = entry.getKey();
            List<ShopOrderEntity> orders = sellOrders.get(currencyName);
            ItemStack icon =  menu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            OrderOption option = new OrderOption(icon, orders);
            temp.offer(option);
        }
        int maxPages = (int) Math.ceil((double) temp.size() / (double) sellOrdersSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, OrderOption> options = new LinkedHashMap<>();
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


    private void displayBuyOptions() {
        Map<List<Integer>, OrderOption> currencyOptions = buyPageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, OrderOption> entry : currencyOptions.entrySet()) {
            List<Integer> coords = entry.getKey();
            OrderOption option = entry.getValue();
            if(option == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack item = option.getIcon().clone(); 
                List<Component> topOrders = new ArrayList<>();
                List<ShopOrderEntity> orders = option.getOrders();
                int count = 0;
                int valid = 0;
                int maxCount = 3;
                Iterator<ShopOrderEntity> iterator = orders.iterator();
                while (iterator.hasNext() && count < maxCount) {
                    ShopOrderEntity element = iterator.next();
                    if(element.getQuantity() - element.getFilledQuantity() > 0) {
                        Component playerComponent = Component.text(Bukkit.getOfflinePlayer(element.getPlayerId()).getName(), NamedTextColor.LIGHT_PURPLE);
                        Component priceComponent = Component.text("Unit Buy Price: ", NamedTextColor.BLUE).append(Component.text(String.format("%.2f", element.getUnitPrice()), NamedTextColor.DARK_GREEN)).append(Component.text(String.format(" %s/unit", element.getCurrencyType()), NamedTextColor.GOLD));
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
                    meta.displayName(Component.text("BUY ORDERS:", NamedTextColor.DARK_AQUA));
                    meta.lore(topOrders);
                }
                item.setItemMeta(meta);
                if(valid > 0) {
                    setItem(coords, item);
                } else {
                    setItem(coords, getLoadingIcon());
                }
            }
        }
    }

    private void displaySellOptions() {
        Map<List<Integer>, OrderOption> currencyOptions = sellPageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, OrderOption> entry : currencyOptions.entrySet()) {
            List<Integer> coords = entry.getKey();
            OrderOption option = entry.getValue();
            if(option == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack item = option.getIcon().clone(); 
                List<Component> topOrders = new ArrayList<>();
                List<ShopOrderEntity> orders = option.getOrders();
                int count = 0;
                int valid = 0;
                int maxCount = 3;
                Iterator<ShopOrderEntity> iterator = orders.iterator();
                while (iterator.hasNext() && count < maxCount) {
                    ShopOrderEntity element = iterator.next();
                    if(element.getQuantity() - element.getFilledQuantity() > 0) {
                        Component playerComponent = Component.text(Bukkit.getOfflinePlayer(element.getPlayerId()).getName(), NamedTextColor.LIGHT_PURPLE);
                        Component priceComponent = Component.text("Unit Sell Price: ", NamedTextColor.BLUE).append(Component.text(String.format("%.2f", element.getUnitPrice()), NamedTextColor.DARK_GREEN)).append(Component.text(String.format(" %s/unit", element.getCurrencyType()), NamedTextColor.GOLD));
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
                    meta.displayName(Component.text("SELL ORDERS:", NamedTextColor.DARK_AQUA));
                    meta.lore(topOrders);
                }
                item.setItemMeta(meta);
                if(valid > 0) {
                    setItem(coords, item);
                } else {
                    setItem(coords, getLoadingIcon());
                }
            }
        }
    }
}
