package com.github.yuqingliu.economy.view.shopmenu.quicksellmenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class QuickSellMenuController {
    private final ShopMenu shopMenu;
    private final int[] quantities = new int[] {1, 4, 8, 16, 32, 64};
    private final int[] sellOptionsStart = new int[]{1,4};
    private final int sellOptionsWidth = 1;
    private final int sellOptionsLength = quantities.length;
    private final int[] itemSlot = new int[]{4,1};
    private final int[] sellInventoryButton = new int[]{7,4};
    private final int[] prevMenuButton = new int[]{2,2};
    private final int[] exitMenuButton = new int[]{6,2};
    private final List<int[]> sellOptions;
    private ShopItemEntity item;
    private OrderOption orderOption;
    private Map<Player, BukkitTask> tasks = new ConcurrentHashMap<>();
    
    public QuickSellMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
        this.sellOptions = shopMenu.rectangleArea(sellOptionsStart, sellOptionsWidth, sellOptionsLength);
    }   

    public void openQuickSellMenu(Inventory inv, ShopItemEntity item, OrderOption orderOption, Player player) {
        this.item = item;
        this.orderOption = orderOption;
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.QuickSellMenu);
        }, Duration.ofMillis(50));
        shopMenu.fill(inv, shopMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        border(inv);
        buttons(inv, player);
        displayItem(inv);
        displaySellOptions(inv, player);
    }

    public void onClose(Player player) {
        if(tasks.containsKey(player)) {
            tasks.get(player).cancel();
            tasks.remove(player);
        }
    }

    public void quickSell(int amount, Player player) {
        Scheduler.runAsync((task) -> {
            int[] data = shopMenu.getShopService().quickSell(item, amount, orderOption.getCurrencyName(), player);
            int filled = amount - data[0];
            double profit = data[1];
            shopMenu.getLogger().sendPlayerNotificationMessage(player, String.format("Sold %d items for %f.2 %s", filled, profit, orderOption.getCurrencyName()));
        });
    }

    private void displayItem(Inventory inv) {
        shopMenu.setItem(inv, itemSlot, item.getIcon().clone());
    }

    private void displaySellOptions(Inventory inv, Player player) {
        int index = 0;
        for(int[] coords : sellOptions) {
            int max = shopMenu.getPluginManager().getInventoryManager().countItemFromPlayer(player, item.getIcon());
            int total = Math.min(max, quantities[index]);
            double profit = 0;
            int qty = total;
            for(ShopOrderEntity order : orderOption.getOrders()) {
                int amount = order.getQuantity() - order.getFilledQuantity();
                if(amount > qty) {
                    profit = qty * order.getUnitPrice();
                    qty = 0;
                    break;
                } else {
                    qty -= amount;
                    profit += amount * order.getUnitPrice();
                }
            }
            int leftover;
            if(total - qty > 0) {
                leftover = total - qty;
            } else {
                leftover = 0;
            }
            Component sell = Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(leftover + "x", NamedTextColor.RED));
            Component profitComponent = Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(profit +"$ ", NamedTextColor.DARK_GREEN).append(orderOption.getIcon().displayName()));
            ItemStack option = shopMenu.createSlotItem(Material.RED_STAINED_GLASS, sell, profitComponent);
            option.setAmount(leftover);
            shopMenu.setItem(inv, coords, option);
            if(leftover == 0) {
                shopMenu.setItem(inv, coords, shopMenu.createSlotItem(Material.BARRIER, sell, profitComponent));
            }
            index++;
        }
    }

    private void border(Inventory inv) {
        ItemStack borderItem = shopMenu.createSlotItem(Material.BLACK_STAINED_GLASS_PANE, shopMenu.getUnavailableComponent());
        shopMenu.fillRectangleArea(inv, new int[]{3,0}, 3, 3, borderItem);
    }

    private void buttons(Inventory inv, Player player) {
        BukkitTask refreshTask = Scheduler.runTimerAsync((task) -> {
            int total = shopMenu.getPluginManager().getInventoryManager().countItemFromPlayer(player, item.getIcon());
            double profit = 0;
            int qty = total;
            for(ShopOrderEntity order : orderOption.getOrders()) {
                int amount = order.getQuantity() - order.getFilledQuantity();
                if(amount > qty) {
                    profit = qty * order.getUnitPrice();
                    qty = 0;
                    break;
                } else {
                    qty -= amount;
                    profit += amount * order.getUnitPrice();
                }
            }
            int leftover;
            if(total - qty > 0) {
                leftover = total - qty;
            } else {
                leftover = 0;
            }
            List<Component> fillLore = Arrays.asList(
                Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(leftover + "x", NamedTextColor.RED)),
                Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(profit +"$ ", NamedTextColor.DARK_GREEN).append(Component.text(orderOption.getCurrencyName(), NamedTextColor.GOLD)))
            );
            ItemStack sellButton = shopMenu.createSlotItem(Material.CHEST, Component.text("Sell Inventory", NamedTextColor.RED), fillLore);
            shopMenu.setItem(inv, sellInventoryButton, sellButton);
        }, Duration.ofSeconds(2),Duration.ofSeconds(0));
        tasks.put(player, refreshTask);
        shopMenu.setItem(inv, prevMenuButton, shopMenu.getPrevMenu());
        shopMenu.setItem(inv, exitMenuButton, shopMenu.getExitMenu());
    }
}
