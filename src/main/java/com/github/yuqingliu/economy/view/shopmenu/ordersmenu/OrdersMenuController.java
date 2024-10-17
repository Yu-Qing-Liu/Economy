package com.github.yuqingliu.economy.view.shopmenu.ordersmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
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
public class OrdersMenuController {
    private final ShopMenu shopMenu;
    private final int prev = 22;
    private final int exit = 31;
    private final int prevBuyOrders = 0;
    private final int nextBuyOrders = 3;
    private final int prevSellOrders = 5;
    private final int nextSellOrders = 8;
    private final int length = 16;
    private final List<Integer> border = Arrays.asList(1,2,4,6,7,13,40,45,46,47,48,49,50,51,52,53);
    private final List<Integer> buttons = Arrays.asList(0,3,5,8,22,31);
    private final List<Integer> buyOrders = Arrays.asList(9,10,11,12,18,19,20,21,27,28,29,30,36,37,38,39);
    private final List<Integer> sellOrders = Arrays.asList(14,15,16,17,23,24,25,26,32,33,34,35,41,42,43,44);
    private Map<Player, PlayerBuyOrdersData> playerBuyOrdersData = new ConcurrentHashMap<>();
    private Map<Player, PlayerSellOrdersData> playerSellOrdersData = new ConcurrentHashMap<>();
    private Material voidOption = Material.GLASS_PANE;
    
    public OrdersMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }

    public void openOrdersMenu(Inventory inv, Player player) {
        playerBuyOrdersData.put(player, new PlayerBuyOrdersData());
        playerSellOrdersData.put(player, new PlayerSellOrdersData());
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.OrdersMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
        frame(inv);
        border(inv);
        Scheduler.runAsync((task) -> {
            fetchBuyOrders(player);
            fetchSellOrders(player);
            displayBuyOrdersOptions(inv, player);
            displaySellOrdersOptions(inv, player);
            pagePtrs(inv);
        });
    }

    public void nextBuyOrdersPage(Inventory inv, Player player) {
        playerBuyOrdersData.get(player).getPageNumber()[0]++;
        if(playerBuyOrdersData.get(player).getBuyOrdersPageData().containsKey(playerBuyOrdersData.get(player).getPageNumber()[0])) {
            displayBuyOrdersOptions(inv, player);
        } else {
            playerBuyOrdersData.get(player).getPageNumber()[0]--;
        }     
    }

    public void prevBuyOrdersPage(Inventory inv, Player player) {
        playerBuyOrdersData.get(player).getPageNumber()[0]--;
        if(playerBuyOrdersData.get(player).getPageNumber()[0] > 0) {
            displayBuyOrdersOptions(inv, player);
        } else {
            playerBuyOrdersData.get(player).getPageNumber()[0]++;
        }
    }

    public void nextSellOrdersPage(Inventory inv, Player player) {
        playerSellOrdersData.get(player).getPageNumber()[0]++;
        if(playerSellOrdersData.get(player).getSellOrdersPageData().containsKey(playerSellOrdersData.get(player).getPageNumber()[0])) {
            displaySellOrdersOptions(inv, player);
        } else {
            playerSellOrdersData.get(player).getPageNumber()[0]--;
        }     
    }

    public void prevSellOrdersPage(Inventory inv, Player player) {
        playerSellOrdersData.get(player).getPageNumber()[0]--;
        if(playerSellOrdersData.get(player).getPageNumber()[0] > 0) {
            displaySellOrdersOptions(inv, player);
        } else {
            playerSellOrdersData.get(player).getPageNumber()[0]++;
        }
    }

    public void onClose(Player player) {
        playerBuyOrdersData.remove(player);
        playerSellOrdersData.remove(player);
    }

    private void displayBuyOrdersOptions(Inventory inv, Player player) {
        PlayerBuyOrdersData data = playerBuyOrdersData.get(player);
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        ShopOrderEntity[] orders = data.getBuyOrdersPageData().getOrDefault(data.getPageNumber()[0], new ShopOrderEntity[length]);
        int currentIndex = 0;
        for(int i : buyOrders) {
            ShopOrderEntity order = orders[currentIndex];
            if(order == null) {
                inv.setItem(i, Placeholder);
            } else {
                ItemStack orderIcon = order.getShopItem().getIcon().clone();
                ItemMeta meta = orderIcon.getItemMeta();
                if(meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    meta.displayName(Component.text("BUY ORDER", NamedTextColor.GOLD));
                    Component nameComponent = Component.text(order.getItemName(), NamedTextColor.AQUA);
                    Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD));
                    Component priceComponent = Component.text("Unit Buy Price: ", NamedTextColor.BLUE).append(Component.text(order.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
                    Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() + "x", NamedTextColor.GREEN));
                    Component quantityBoughtComponent = Component.text("Quantity Bought: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x", NamedTextColor.GREEN));
                    meta.lore(Arrays.asList(nameComponent, currencyComponent, priceComponent, quantityComponent, quantityBoughtComponent));
                }
                orderIcon.setItemMeta(meta);
                inv.setItem(i, orderIcon);
            }
            currentIndex++;
        }
    }

    private void displaySellOrdersOptions(Inventory inv, Player player) {
        PlayerSellOrdersData data = playerSellOrdersData.get(player);
        ItemStack Placeholder = new ItemStack(voidOption);
        ItemMeta pmeta = Placeholder.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(pmeta);
        ShopOrderEntity[] orders = data.getSellOrdersPageData().getOrDefault(data.getPageNumber()[0], new ShopOrderEntity[length]);
        int currentIndex = 0;
        for(int i : sellOrders) {
            ShopOrderEntity order = orders[currentIndex];
            if(order == null) {
                inv.setItem(i, Placeholder);
            } else {
                ItemStack orderIcon = order.getShopItem().getIcon().clone();
                ItemMeta meta = orderIcon.getItemMeta();
                if(meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    meta.displayName(Component.text("SELL ORDER", NamedTextColor.GOLD));
                    Component nameComponent = Component.text(order.getItemName(), NamedTextColor.AQUA);
                    Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD));
                    Component priceComponent = Component.text("Unit Buy Price: ", NamedTextColor.BLUE).append(Component.text(order.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
                    Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() + "x", NamedTextColor.GREEN));
                    Component quantityBoughtComponent = Component.text("Quantity Sold: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x", NamedTextColor.GREEN));
                    meta.lore(Arrays.asList(nameComponent, currencyComponent, priceComponent, quantityComponent, quantityBoughtComponent));
                }
                orderIcon.setItemMeta(meta);
                inv.setItem(i, orderIcon);
            }
            currentIndex++;
        }
    }

    private void fetchBuyOrders(Player player) {
        PlayerBuyOrdersData buyOrdersData = playerBuyOrdersData.get(player);
        List<ShopOrderEntity> buyOrders = shopMenu.getShopService().getPlayerBuyOrders(player);
        Queue<ShopOrderEntity> tempBuyOrders = new ArrayDeque<>();
        tempBuyOrders.addAll(buyOrders);
        int maxBuyOrdersPages = (int) Math.ceil((double) tempBuyOrders.size() / (double) length);
        for (int i = 0; i < maxBuyOrdersPages; i++) {
            int pageNum = i + 1;
            ShopOrderEntity[] options = new ShopOrderEntity[length];
            for (int j = 0; j < length; j++) {
                if(tempBuyOrders.isEmpty()) {
                    options[j] = null;
                } else {
                    options[j] = tempBuyOrders.poll();
                }
            }
            buyOrdersData.getBuyOrdersPageData().put(pageNum, options);
        }
    }

    private void fetchSellOrders(Player player) {
        PlayerSellOrdersData sellOrdersData = playerSellOrdersData.get(player);
        List<ShopOrderEntity> sellOrders = shopMenu.getShopService().getPlayerSellOrders(player);
        Queue<ShopOrderEntity> tempSellOrders = new ArrayDeque<>();
        tempSellOrders.addAll(sellOrders);
        int maxsellOrdersPages = (int) Math.ceil((double) tempSellOrders.size() / (double) length);
        for (int i = 0; i < maxsellOrdersPages; i++) {
            int pageNum = i + 1;
            ShopOrderEntity[] options = new ShopOrderEntity[length];
            for (int j = 0; j < length; j++) {
                if(tempSellOrders.isEmpty()) {
                    options[j] = null;
                } else {
                    options[j] = tempSellOrders.poll();
                }
            }
            sellOrdersData.getSellOrdersPageData().put(pageNum, options);
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

    private void pagePtrs(Inventory inv) {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nmeta = nextPage.getItemMeta();
        if(nmeta != null) {
            nmeta.displayName(Component.text("Next Page", NamedTextColor.AQUA));
        }
        nextPage.setItemMeta(nmeta);
        inv.setItem(nextBuyOrders, nextPage);
        inv.setItem(nextSellOrders, nextPage);

        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta pmeta = prevPage.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Previous Page", NamedTextColor.AQUA));
        }
        prevPage.setItemMeta(pmeta);
        inv.setItem(prevBuyOrders, prevPage);
        inv.setItem(prevSellOrders, prevPage);

        ItemStack prev = new ItemStack(Material.GREEN_WOOL);
        ItemMeta prevmeta = prev.getItemMeta();
        if(prevmeta != null) {
            prevmeta.displayName(Component.text("Sections", NamedTextColor.GREEN));
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
