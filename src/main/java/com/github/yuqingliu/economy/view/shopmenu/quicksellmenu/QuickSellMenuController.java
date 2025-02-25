package com.github.yuqingliu.economy.view.shopmenu.quicksellmenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderOption;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class QuickSellMenuController extends AbstractPlayerInventoryController<ShopMenu> {
    private final int[] quantities = new int[] {1, 4, 8, 16, 32, 64};
    private final int[] sellOptionsStart = new int[]{1,4};
    private final int sellOptionsWidth = 1;
    private final int sellOptionsLength = quantities.length;
    private final int[] itemSlot = new int[]{4,1};
    private final int[] sellInventoryButton = new int[]{7,4};
    private final int[] prevMenuButton = new int[]{0,0};
    private final int[] refreshButton = new int[]{1,0};
    private final int[] exitMenuButton = new int[]{2,0};
    private final List<int[]> sellOptions;
    private ShopItemEntity item;
    private OrderOption orderOption;
    
    public QuickSellMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
        this.sellOptions = rectangleArea(sellOptionsStart, sellOptionsWidth, sellOptionsLength);
    }   

    public void openMenu(ShopItemEntity item, OrderOption orderOption) {
        this.item = item;
        this.orderOption = orderOption;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.QuickSellMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        displayItem();
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(refreshButton, getReloadIcon());
        reload();
    }

    public void reload() {
        Scheduler.runAsync(t -> {
            buttons();
        });
    }

    public void quickSell(int amount) {
        Scheduler.runAsync((task) -> {
            double[] cdata = computeRemainderAndProfit(amount);
            int sellAmount = (int) cdata[0];
            int[] data = menu.getShopService().quickSell(item, sellAmount, orderOption.getCurrencyName(), player);
            int filled = sellAmount - data[0];
            double profit = data[1];
            if(profit > 0) {
                menu.getLogger().sendPlayerNotificationMessage(player, String.format("Sold %d items for %.2f %s", filled, profit, orderOption.getCurrencyName()));
            } else {
                menu.getLogger().sendPlayerErrorMessage(player, "No more offers.");
            }
        });
    }

    private void displayItem() {
        setItem(itemSlot, item.getIcon().clone());
    }

    private double[] computeRemainderAndProfit(int quantity) {
        int max = menu.getPluginManager().getInventoryManager().countItemFromPlayer(player, item.getIcon());
        int total = Math.min(max, quantity);
        double profit = 0;
        int qty = total;
        for(ShopOrderEntity order : orderOption.getOrders()) {
            int amount = order.getQuantity() - order.getFilledQuantity();
            if(amount > qty) {
                profit += qty * order.getUnitPrice();
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
        return new double[] {leftover, profit};
    }

    private void displaySellOptions() {
        int index = 0;
        for(int[] coords : sellOptions) {
            double[] data = computeRemainderAndProfit(quantities[index]);
            int leftover = (int) data[0];
            double profit = data[1];
            Component sell = Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(leftover + "x", NamedTextColor.RED));
            Component profitComponent = Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(profit, NamedTextColor.DARK_GREEN).append(Component.text(" " + orderOption.getCurrencyName(), NamedTextColor.GOLD)));
            ItemStack option = createSlotItem(Material.RED_STAINED_GLASS, sell, profitComponent);
            option.setAmount(leftover);
            setItem(coords, option);
            if(leftover == 0) {
                setItem(coords, createSlotItem(Material.BARRIER, sell, profitComponent));
            }
            index++;
        }
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{3,0}, 3, 3, borderItem);
    }

    private void buttons() {
        displaySellOptions();
        int total = menu.getPluginManager().getInventoryManager().countItemFromPlayer(player, item.getIcon());
        double[] data = computeRemainderAndProfit(total);
        int leftover = (int) data[0];
        double profit = data[1];
        List<Component> fillLore = Arrays.asList(
            Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(leftover + "x", NamedTextColor.RED)),
            Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(profit, NamedTextColor.DARK_GREEN).append(Component.text(" " + orderOption.getCurrencyName(), NamedTextColor.GOLD)))
        );
        ItemStack sellButton = createSlotItem(Material.CHEST, Component.text("Sell Inventory", NamedTextColor.RED), fillLore);
        setItem(sellInventoryButton, sellButton);
    }
}
