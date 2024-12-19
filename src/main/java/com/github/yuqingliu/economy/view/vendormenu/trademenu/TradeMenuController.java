package com.github.yuqingliu.economy.view.vendormenu.trademenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;
import com.github.yuqingliu.economy.view.vendormenu.transactionmenu.CurrencyOption;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class TradeMenuController extends AbstractPlayerInventoryController<VendorMenu> {
    private final int[] quantities = new int[] {1, 4, 8, 16, 32, 64};
    private final int[] buyOptionsStart = new int[]{1,3};
    private final int buyOptionsWidth = 1;
    private final int buyOptionsLength = quantities.length;
    private final int[] sellOptionsStart = new int[]{1,4};
    private final int sellOptionsWidth = 1;
    private final int sellOptionsLength = quantities.length;
    private final int[] itemSlot = new int[]{4,1};
    private final int[] buyInventoryButton = new int[]{7,3};
    private final int[] sellInventoryButton = new int[]{7,4};
    private final int[] prevMenuButton = new int[]{2,2};
    private final int[] exitMenuButton = new int[]{6,2};
    private final List<int[]> buyOptions;
    private final List<int[]> sellOptions;
    private VendorItemEntity item;
    private CurrencyOption currencyOption;
    private BukkitTask task;
    
    public TradeMenuController(Player player, Inventory inventory, VendorMenu vendorMenu) {
        super(player, inventory, vendorMenu);
        this.buyOptions = rectangleArea(buyOptionsStart, buyOptionsWidth, buyOptionsLength);
        this.sellOptions = rectangleArea(sellOptionsStart, sellOptionsWidth, sellOptionsLength);
    }   

    public void openMenu(VendorItemEntity item, CurrencyOption currencyOption) {
        this.item = item;
        this.currencyOption = currencyOption;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.TradeMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayItem();
        displayBuyOptions();
        displaySellOptions();
    }

    public void buy(int slotAmount) {
        Scheduler.runAsync((task) -> {
            menu.getVendorService().buy(item, slotAmount, currencyOption.getCurrencyName(), player);
        });
    }

    public void sell(int slotAmount) {
        Scheduler.runAsync((task) -> {
            int amount = Math.min(menu.getPluginManager().getInventoryManager().countItemFromPlayer(player, item.getIcon().clone()), slotAmount);
            menu.getVendorService().sell(item, amount, currencyOption.getCurrencyName(), player);
        });
    }

    public void onClose() {
        if(task != null) {
            task.cancel();
        }
    }

    private void displayItem() {
        setItem(itemSlot, item.getIcon().clone());
    }

    private void buttons() {
        task = Scheduler.runTimerAsync((task) -> {
            int freeSpace = menu.getPluginManager().getInventoryManager().countAvailableInventorySpace(player, item.getIcon().getType());
            int amount = menu.getPluginManager().getInventoryManager().countItemFromPlayer(player, item.getIcon());
            List<Component> fillLore = Arrays.asList(
                Component.text("BUY: ", NamedTextColor.GOLD).append(Component.text(freeSpace + "x", NamedTextColor.RED)),
                Component.text("COST: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getBuyPrice(freeSpace) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()))
            );
            List<Component> sellLore = Arrays.asList(
                Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(amount + "x", NamedTextColor.RED)),
                Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getSellPrice(amount) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()))
            );
            ItemStack fillButton = createSlotItem(Material.CHEST, Component.text("Fill Inventory", NamedTextColor.RED), fillLore);
            ItemStack sellButton = createSlotItem(Material.CHEST, Component.text("Sell Inventory", NamedTextColor.RED), sellLore);
            setItem(buyInventoryButton, fillButton);
            setItem(sellInventoryButton, sellButton);
        }, Duration.ofSeconds(2),Duration.ofSeconds(0));
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{3,0}, 3, 3, borderItem);
    }

    private void displayBuyOptions() {
        int index = 0;
        for(int[] coords : buyOptions) {
            Component buy = Component.text("BUY: ", NamedTextColor.GOLD).append(Component.text(quantities[index] + "x", NamedTextColor.RED));
            Component cost = Component.text("COST: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getBuyPrice(quantities[index]) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()));
            ItemStack option = createSlotItem(Material.LIME_STAINED_GLASS, buy, cost);
            option.setAmount(quantities[index]);
            setItem(coords, option);
            index++;
        }
    }

    private void displaySellOptions() {
        int index = 0;
        for(int[] coords : sellOptions) {
            Component sell = Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(quantities[index] + "x", NamedTextColor.RED));
            Component profit = Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getSellPrice(quantities[index]) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()));
            ItemStack option = createSlotItem(Material.RED_STAINED_GLASS, sell, profit);
            option.setAmount(quantities[index]);
            setItem(coords, option);
            index++;
        }
    }
}
