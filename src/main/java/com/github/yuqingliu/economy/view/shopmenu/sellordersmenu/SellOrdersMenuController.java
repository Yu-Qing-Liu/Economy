package com.github.yuqingliu.economy.view.shopmenu.sellordersmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class SellOrdersMenuController {
    private final ShopMenu shopMenu;
    private final int[] prevMenuButton = new int[]{1,2};
    private final int[] exitMenuButton = new int[]{1,3};
    private final int[] reloadButton = new int[]{1,4};
    private final int[] nextSellOrdersButton = new int[]{7,2};
    private final int[] prevSellOrdersButton = new int[]{7,3};
    private final int[] sellOrdersStart = new int[]{2,1};
    private final int sellOrdersLength = 5;
    private final int sellOrdersWidth = 4;
    private final int sellOrdersSize = sellOrdersLength * sellOrdersWidth;
    private final List<int[]> sellOrders;
    private Map<Player, Map<Integer, Map<List<Integer>, ShopOrderEntity>>> pageData = new ConcurrentHashMap<>();
    private Map<Player, int[]> pageNumbers = new ConcurrentHashMap<>();
    
    public SellOrdersMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.sellOrders = shopMenu.rectangleArea(sellOrdersStart, sellOrdersWidth, sellOrdersLength);
    }

    public void openSellOrdersMenu(Inventory inv, Player player) {
        pageNumbers.put(player, new int[]{1});
        pageData.put(player, new ConcurrentHashMap<>());
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.SellOrdersMenu);
        }, Duration.ofMillis(50));
        shopMenu.fill(inv, shopMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        border(inv);
        buttons(inv);
        reload(inv, player);
    }

    public void reload(Inventory inv, Player player) {
        shopMenu.rectangleAreaLoading(inv, sellOrdersStart, sellOrdersWidth, sellOrdersLength);
        Scheduler.runAsync((task) -> {
            fetchSellOrders(player);
            displaySellOrdersOptions(inv, player);
        });
    }

    public void nextSellOrdersPage(Inventory inv, Player player) {
        pageNumbers.get(player)[0]++;
        if(pageData.get(player).containsKey(pageNumbers.get(player)[0])) {
            displaySellOrdersOptions(inv, player);
        } else {
            pageNumbers.get(player)[0]--;
        }     
    }

    public void prevSellOrdersPage(Inventory inv, Player player) {
        pageNumbers.get(player)[0]--;
        if(pageNumbers.get(player)[0] > 0) {
            displaySellOrdersOptions(inv, player);
        } else {
            pageNumbers.get(player)[0]++;
        }
    }

    public void onClose(Player player) {
        pageNumbers.remove(player);
        pageData.remove(player);
    }

    private void fetchSellOrders(Player player) {
        List<ShopOrderEntity> sellOrders = shopMenu.getShopService().getPlayerSellOrders(player);
        Queue<ShopOrderEntity> tempSellOrders = new ArrayDeque<>();
        tempSellOrders.addAll(sellOrders);
        int maxSellOrdersPages = (int) Math.ceil((double) tempSellOrders.size() / (double) sellOrdersSize);
        for (int i = 0; i < maxSellOrdersPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, ShopOrderEntity> options = new LinkedHashMap<>();
            for (int[] coords : this.sellOrders) {
                if(tempSellOrders.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), tempSellOrders.poll());
                }
            }
            pageData.get(player).put(pageNum, options);
        }
    }

    private void displaySellOrdersOptions(Inventory inv, Player player) {
        Map<Integer, Map<List<Integer>, ShopOrderEntity>> playerData = pageData.get(player);
        Map<List<Integer>, ShopOrderEntity> orders = playerData.getOrDefault(pageNumbers.get(player)[0], Collections.emptyMap());
        for(Map.Entry<List<Integer>, ShopOrderEntity> entry : orders.entrySet()) {
            List<Integer> coords = entry.getKey();
            ShopOrderEntity order = entry.getValue();
            if(order == null) {
                shopMenu.setItem(inv, coords, shopMenu.getUnavailable());
            } else {
                ItemStack orderIcon = order.getShopItem().getIcon().clone();
                ItemMeta meta = orderIcon.getItemMeta();
                if(meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    meta.displayName(Component.text("SELL ORDER", NamedTextColor.GOLD));
                    Component nameComponent = Component.text(order.getItemName(), NamedTextColor.AQUA);
                    Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD));
                    Component priceComponent = Component.text("Unit Sell Price: ", NamedTextColor.BLUE).append(Component.text(order.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
                    Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() + "x", NamedTextColor.GREEN));
                    Component quantityBoughtComponent = Component.text("Quantity Bought: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x", NamedTextColor.GREEN));
                    meta.lore(Arrays.asList(nameComponent, currencyComponent, priceComponent, quantityComponent, quantityBoughtComponent));
                }
                orderIcon.setItemMeta(meta);
                shopMenu.setItem(inv, coords, orderIcon);
            }
        }
    }

    private void border(Inventory inv) {
        ItemStack borderItem = shopMenu.createSlotItem(Material.BLACK_STAINED_GLASS_PANE, shopMenu.getUnavailableComponent());
        int[] b1 = new int[]{1,1};
        int[] b3 = new int[]{7,1};
        int[] b4 = new int[]{7,4};
        shopMenu.setItem(inv, b1, borderItem);
        shopMenu.setItem(inv, b3, borderItem);
        shopMenu.setItem(inv, b4, borderItem);
    }

    private void buttons(Inventory inv) {
        shopMenu.setItem(inv, prevMenuButton, shopMenu.getPrevMenu());
        shopMenu.setItem(inv, exitMenuButton, shopMenu.getExitMenu());
        shopMenu.setItem(inv, nextSellOrdersButton, shopMenu.getNextPage());
        shopMenu.setItem(inv, prevSellOrdersButton, shopMenu.getPrevPage());
        shopMenu.setItem(inv, reloadButton, shopMenu.getReload());
    }
}
