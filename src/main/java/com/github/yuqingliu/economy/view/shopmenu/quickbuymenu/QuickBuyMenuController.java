package com.github.yuqingliu.economy.view.shopmenu.quickbuymenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

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
public class QuickBuyMenuController extends AbstractPlayerInventoryController<ShopMenu> {
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
    private BukkitTask task;
    
    public QuickBuyMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
        this.buyOptions = rectangleArea(buyOptionsStart, buyOptionsWidth, buyOptionsLength);
    }   

    public void openMenu(ShopItemEntity item, OrderOption orderOption) {
        this.item = item;
        this.orderOption = orderOption;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.QuickBuyMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayItem();
        displayBuyOptions();
    }

    public void onClose() {
        if(task != null) {
            task.cancel();
        }
    }

    public void quickBuy(int amount) {
        Scheduler.runAsync((task) -> {
            int[] data = menu.getShopService().quickBuy(item, amount, orderOption.getCurrencyName(), player);
            int filled = amount - data[0];
            double cost = data[1];
            if(cost > 0) {
                menu.getLogger().sendPlayerNotificationMessage(player, String.format("Bought %d items for %.2f %s", filled, cost, orderOption.getCurrencyName()));
            } else {
                menu.getLogger().sendPlayerErrorMessage(player, "No more offers");
            }
        });
    }

    private void displayItem() {
        setItem(itemSlot, item.getIcon().clone());
    }

    private void displayBuyOptions() {
        int index = 0;
        for(int[] coords : buyOptions) {
            double cost = 0;
            int qty = quantities[index];
            for(ShopOrderEntity order : orderOption.getOrders()) {
                int amount = order.getQuantity() - order.getFilledQuantity();
                if(amount > qty) {
                    cost += qty * order.getUnitPrice();
                    qty = 0;
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
            ItemStack option = createSlotItem(Material.LIME_STAINED_GLASS, buy, costComponent);
            option.setAmount(leftover);
            setItem(coords, option);
            index++;
        }
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{3,0}, 3, 3, borderItem);
    }

    private void buttons() {
        task = Scheduler.runTimerAsync((task) -> {
            displayBuyOptions();
            int freeSpace = menu.getPluginManager().getInventoryManager().countAvailableInventorySpace(player, item.getIcon().getType());
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
            ItemStack fillButton = createSlotItem(Material.CHEST, Component.text("Fill Inventory", NamedTextColor.RED), fillLore);
            setItem(buyInventoryButton, fillButton);
        }, Duration.ofSeconds(2),Duration.ofSeconds(0));
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
    }
}
