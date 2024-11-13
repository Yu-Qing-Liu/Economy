package com.github.yuqingliu.economy.view.shopmenu.quickbuymenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderOption;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class QuickBuyMenuController {
    private final ShopMenu shopMenu;
    private final int[] quantities = new int[] {1, 4, 8, 16, 32, 64};
    private final int[] buyOptionsStart = new int[]{1,4};
    private final int buyOptionsWidth = 1;
    private final int buyOptionsLength = quantities.length;
    private final int[] itemSlot = new int[]{4,1};
    private final int[] buyInventoryButton = new int[]{7,4};
    private final int[] prevMenuButton = new int[]{2,2};
    private final int[] exitMenuButton = new int[]{6,2};
    private final List<int[]> buyOptions;
    private ShopItemEntity item;
    private OrderOption orderOption;
    private Map<Player, BukkitTask> tasks = new ConcurrentHashMap<>();
    
    public QuickBuyMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.buyOptions = shopMenu.rectangleArea(buyOptionsStart, buyOptionsWidth, buyOptionsLength);
    }   

    public void openQuickBuyMenu(Inventory inv, ShopItemEntity item, OrderOption orderOption, Player player) {
        this.item = item;
        this.orderOption = orderOption;
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.QuickBuyMenu);
        }, Duration.ofMillis(50));
        shopMenu.fill(inv, shopMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        border(inv);
        buttons(inv, player);
        displayItem(inv);
        displayBuyOptions(inv);
    }

    public void onClose(Player player) {
        if(tasks.containsKey(player)) {
            tasks.get(player).cancel();
            tasks.remove(player);
        }
    }

    public void quickBuy(int amount, Player player) {
        Scheduler.runAsync((task) -> {
            shopMenu.getShopService().quickBuy();
        });
    }

    private void displayItem(Inventory inv) {
        shopMenu.setItem(inv, itemSlot, item.getIcon().clone());
    }

    private void displayBuyOptions(Inventory inv) {
        int index = 0;
        for(int[] coords : buyOptions) {
            double cost = 0;
            int qty = quantities[index];
            for(ShopOrderEntity order : orderOption.getOrders()) {
                int amount = order.getQuantity() - order.getFilledQuantity();
                if(amount > qty) {
                    cost = qty * order.getUnitPrice();
                    break;
                } else {
                    qty -= amount;
                    cost += amount * order.getUnitPrice();
                }
            }
            int leftover;
            if(quantities[index] - qty > 0) {
                leftover = quantities[index] - qty;
            } else {
                leftover = quantities[index];
            }
            Component buy = Component.text("BUY: ", NamedTextColor.GOLD).append(Component.text(leftover + "x", NamedTextColor.RED));
            Component costComponent = Component.text("COST: ", NamedTextColor.DARK_PURPLE).append(Component.text(cost +"$ ", NamedTextColor.DARK_GREEN).append(orderOption.getIcon().displayName()));
            ItemStack option = shopMenu.createSlotItem(Material.LIME_STAINED_GLASS, buy, costComponent);
            option.setAmount(leftover);
            shopMenu.setItem(inv, coords, option);
            index++;
        }
    }

    private void border(Inventory inv) {
        ItemStack borderItem = shopMenu.createSlotItem(Material.BLACK_STAINED_GLASS_PANE, shopMenu.getUnavailableComponent());
        shopMenu.fillRectangleArea(inv, new int[]{3,0}, 3, 3, borderItem);
    }

    private void buttons(Inventory inv, Player player) {
        BukkitTask refreshTask = Scheduler.runTimerAsync((task) -> {
            int freeSpace = shopMenu.countAvailableInventorySpace(player, item.getIcon().getType());
            double cost = 0;
            int qty = freeSpace;
            for(ShopOrderEntity order : orderOption.getOrders()) {
                int amount = order.getQuantity() - order.getFilledQuantity();
                if(amount > qty) {
                    cost = qty * order.getUnitPrice();
                    break;
                } else {
                    qty -= amount;
                    cost += amount * order.getUnitPrice();
                }
            }
            int leftover;
            if(freeSpace - qty > 0) {
                leftover = freeSpace - qty;
            } else {
                leftover = 0;
            }
            List<Component> fillLore = Arrays.asList(
                Component.text("BUY: ", NamedTextColor.GOLD).append(Component.text(leftover + "x", NamedTextColor.RED)),
                Component.text("COST: ", NamedTextColor.DARK_PURPLE).append(Component.text(cost +"$ ", NamedTextColor.DARK_GREEN).append(Component.text(orderOption.getCurrencyName(), NamedTextColor.GOLD)))
            );
            ItemStack fillButton = shopMenu.createSlotItem(Material.CHEST, Component.text("Fill Inventory", NamedTextColor.RED), fillLore);
            shopMenu.setItem(inv, buyInventoryButton, fillButton);
        }, Duration.ofSeconds(2),Duration.ofSeconds(0));
        tasks.put(player, refreshTask);
        shopMenu.setItem(inv, prevMenuButton, shopMenu.getPrevMenu());
        shopMenu.setItem(inv, exitMenuButton, shopMenu.getExitMenu());
    }
}
