package com.github.yuqingliu.economy.view.vendormenu.trademenu;

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
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;
import com.github.yuqingliu.economy.view.vendormenu.transactionmenu.CurrencyOption;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class TradeMenuController {
    private final VendorMenu vendorMenu;
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
    private Map<Player, BukkitTask> tasks = new ConcurrentHashMap<>();
    
    public TradeMenuController(VendorMenu vendorMenu) {
        this.vendorMenu = vendorMenu;
        this.buyOptions = vendorMenu.rectangleArea(buyOptionsStart, buyOptionsWidth, buyOptionsLength);
        this.sellOptions = vendorMenu.rectangleArea(sellOptionsStart, sellOptionsWidth, sellOptionsLength);
    }   

    public void openTradeMenu(Inventory inv, VendorItemEntity item, CurrencyOption currencyOption, Player player) {
        this.item = item;
        this.currencyOption = currencyOption;
        Scheduler.runLaterAsync((task) -> {
            vendorMenu.getPlayerMenuTypes().put(player, MenuType.TradeMenu);
        }, Duration.ofMillis(50));
        vendorMenu.clear(inv);
        vendorMenu.fill(inv, vendorMenu.getBackgroundItems().get(Material.BLUE_STAINED_GLASS_PANE));
        border(inv);
        buttons(inv, player);
        displayItem(inv);
        displayBuyOptions(inv);
        displaySellOptions(inv);
    }

    public void buy(Player player, int slotAmount) {
        Scheduler.runAsync((task) -> {
            double cost = slotAmount * currencyOption.getBuyPrice();
            boolean successfulWithdrawal = vendorMenu.getCurrencyService().withdrawPlayerPurse(player, currencyOption.getCurrencyName(), cost);
            if(!successfulWithdrawal) {
                vendorMenu.getLogger().sendPlayerErrorMessage(player, "Not enough currency.");
                return;
            }
            vendorMenu.addItemToPlayer(player, item.getIcon().clone(), slotAmount);
            vendorMenu.getLogger().sendPlayerNotificationMessage(player, String.format("Bought %d item(s) for %.2f %s", slotAmount, cost, currencyOption.getCurrencyName()));
            vendorMenu.getSoundManager().playTransactionSound(player);
        });
    }

    public void sell(Player player, int slotAmount) {
        Scheduler.runAsync((task) -> {
            int amount = Math.min(vendorMenu.countItemFromPlayer(player, item.getIcon().clone()), slotAmount);
            double profit = amount * currencyOption.getSellPrice();
            boolean sucessfulItemRemoval = vendorMenu.removeItemFromPlayer(player, item.getIcon().clone(), amount);
            if(!sucessfulItemRemoval) {
                vendorMenu.getLogger().sendPlayerErrorMessage(player, "Not enough items to sell.");
                return;
            }
            boolean sucessfulDeposit = vendorMenu.getCurrencyService().depositPlayerPurse(player, currencyOption.getCurrencyName(), profit);
            if(!sucessfulDeposit) {
                vendorMenu.getLogger().sendPlayerErrorMessage(player, "Could not sell item(s)");
                vendorMenu.addItemToPlayer(player, item.getIcon().clone(), amount);
            }
            vendorMenu.getLogger().sendPlayerNotificationMessage(player, String.format("Sold %d item(s) for %.2f %s", amount, profit, currencyOption.getCurrencyName()));
            vendorMenu.getSoundManager().playTransactionSound(player);
        });
    }

    public void onClose(Player player) {
        tasks.get(player).cancel();
        tasks.remove(player);
    }

    private void displayItem(Inventory inv) {
        vendorMenu.setItem(inv, itemSlot, item.getIcon().clone());
    }

    private void buttons(Inventory inv, Player player) {
        BukkitTask refreshTask = Scheduler.runTimerAsync((task) -> {
            int freeSpace = vendorMenu.countAvailableInventorySpace(player, item.getIcon().getType());
            int amount = vendorMenu.countItemFromPlayer(player, item.getIcon());
            List<Component> fillLore = Arrays.asList(
                Component.text("BUY: ", NamedTextColor.GOLD).append(Component.text(freeSpace + "x", NamedTextColor.RED)),
                Component.text("COST: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getBuyPrice(freeSpace) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()))
            );
            List<Component> sellLore = Arrays.asList(
                Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(amount + "x", NamedTextColor.RED)),
                Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getSellPrice(amount) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()))
            );
            ItemStack fillButton = vendorMenu.createSlotItem(Material.CHEST, Component.text("Fill Inventory", NamedTextColor.RED), fillLore);
            ItemStack sellButton = vendorMenu.createSlotItem(Material.CHEST, Component.text("Sell Inventory", NamedTextColor.RED), sellLore);
            vendorMenu.setItem(inv, buyInventoryButton, fillButton);
            vendorMenu.setItem(inv, sellInventoryButton, sellButton);
        }, Duration.ofSeconds(2),Duration.ofSeconds(0));
        tasks.put(player, refreshTask);
        vendorMenu.setItem(inv, prevMenuButton, vendorMenu.getPrevMenu());
        vendorMenu.setItem(inv, exitMenuButton, vendorMenu.getExitMenu());
    }

    private void border(Inventory inv) {
        ItemStack borderItem = vendorMenu.createSlotItem(Material.BLACK_STAINED_GLASS_PANE, vendorMenu.getUnavailableComponent());
        vendorMenu.fillRectangleArea(inv, new int[]{3,0}, 3, 3, borderItem);
    }

    private void displayBuyOptions(Inventory inv) {
        int index = 0;
        for(int[] coords : buyOptions) {
            Component buy = Component.text("BUY: ", NamedTextColor.GOLD).append(Component.text(quantities[index] + "x", NamedTextColor.RED));
            Component cost = Component.text("COST: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getBuyPrice(quantities[index]) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()));
            ItemStack option = vendorMenu.createSlotItem(Material.LIME_STAINED_GLASS, buy, cost);
            option.setAmount(quantities[index]);
            vendorMenu.setItem(inv, coords, option);
            index++;
        }
    }

    private void displaySellOptions(Inventory inv) {
        int index = 0;
        for(int[] coords : sellOptions) {
            Component sell = Component.text("SELL: ", NamedTextColor.GOLD).append(Component.text(quantities[index] + "x", NamedTextColor.RED));
            Component profit = Component.text("PROFIT: ", NamedTextColor.DARK_PURPLE).append(Component.text(currencyOption.getSellPrice(quantities[index]) +"$ ", NamedTextColor.DARK_GREEN).append(currencyOption.getIcon().displayName()));
            ItemStack option = vendorMenu.createSlotItem(Material.RED_STAINED_GLASS, sell, profit);
            option.setAmount(quantities[index]);
            vendorMenu.setItem(inv, coords, option);
            index++;
        }
    }
}
