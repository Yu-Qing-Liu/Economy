package com.github.yuqingliu.economy.view.shopmenu.sellordermenu;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.buyordermenu.PlayerData;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderMenuController;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class SellOrderMenuController extends AbstractPlayerInventoryController<ShopMenu> {
    private final int[] prevMenuButton = new int[]{2,1};
    private final int[] itemSlot = new int[]{4,1};
    private final int[] exitMenuButton = new int[]{6,1};
    private final int[] setCurrencyTypeButton = new int[]{1,3};
    private final int[] setQuantityButton = new int[]{3,3};
    private final int[] setPriceButton = new int[]{5,3};
    private final int[] orderInfo = new int[]{7,3};
    private final int[] currencySlot = new int[]{1,4};
    private final int[] quantitySlot = new int[]{3,4};
    private final int[] priceSlot = new int[]{5,4};
    private final int[] confirmOrderButton = new int[]{7,4};
    @Setter private PlayerData playerData = new PlayerData();
    private ShopItemEntity item;
    private BukkitTask task;
    
    public SellOrderMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
    }

    public void openMenu(ShopItemEntity item) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.SellOrderMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayItem();
        task = Scheduler.runTimerAsync((task) -> {
            orderInfo();
            results();
        }, Duration.ofSeconds(1), Duration.ofSeconds(0));
    }

    public void setCurrencyType() {
        inventory.close();
        PlayerInventory shop = menu.getPluginManager().getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            Inventory inventory = shop.load(player);
            SellOrderMenuController controller = menu.getSellOrderMenu().getControllers().getPlayerInventoryController(player, new SellOrderMenuController(player, inventory, menu));
            controller.openMenu(item);
            Scheduler.runAsync((task) -> {
                CurrencyEntity curr = menu.getCurrencyService().getCurrencyByName(userInput);
                if (curr != null) {
                    ItemStack icon = curr.getIcon().clone();
                    controller.setPlayerData(playerData);
                    controller.getPlayerData().setCurrencyTypeInput(userInput);
                    controller.getPlayerData().setCurrencyTypeIcon(icon);
                    controller.setItem(currencySlot, icon);
                }
            });
        };        

        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("currency", NamedTextColor.RED));
        scanner.open(player);
    }

    public void setQuantity() {
        inventory.close();
        PlayerInventory shop = menu.getPluginManager().getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            Inventory inventory = shop.load(player);
            SellOrderMenuController controller = menu.getSellOrderMenu().getControllers().getPlayerInventoryController(player, new SellOrderMenuController(player, inventory, menu));
            controller.openMenu(item);
            try {
                int quantityInput = Integer.parseInt(userInput);
                ItemStack quantityIcon = new ItemStack(Material.PAPER);
                ItemMeta meta = quantityIcon.getItemMeta();
                if(meta != null) {
                    meta.displayName(Component.text(String.format("%sx", userInput), NamedTextColor.DARK_GREEN));
                }
                quantityIcon.setItemMeta(meta);
                controller.setPlayerData(playerData);
                controller.getPlayerData().setQuantityIcon(quantityIcon);
                controller.getPlayerData().setQuantityInput(quantityInput);
                controller.setItem(quantitySlot, quantityIcon);
            } catch (Exception e) {}
        };        

        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("quantity", NamedTextColor.RED));
        scanner.open(player);
    }

    public void setUnitPrice() {
        inventory.close();
        PlayerInventory shop = menu.getPluginManager().getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            Inventory inventory = shop.load(player);
            SellOrderMenuController controller = menu.getSellOrderMenu().getControllers().getPlayerInventoryController(player, new SellOrderMenuController(player, inventory, menu));
            controller.openMenu(item);
            try {
                double unitPriceInput = Double.parseDouble(userInput);
                ItemStack unitPriceIcon = new ItemStack(Material.PAPER);
                ItemMeta meta = unitPriceIcon.getItemMeta();
                if(meta != null) {
                    meta.displayName(Component.text(String.format("%s $/unit", userInput), NamedTextColor.DARK_GREEN));
                }
                unitPriceIcon.setItemMeta(meta);
                controller.setPlayerData(playerData);
                controller.getPlayerData().setUnitPriceIcon(unitPriceIcon);
                controller.getPlayerData().setUnitPriceInput(unitPriceInput);
                controller.setItem(priceSlot, unitPriceIcon);
            } catch (Exception e) {}
        };        

        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("unit price", NamedTextColor.RED));
        scanner.open(player);
    }

    public void confirmOrder() {
        Scheduler.runAsync((task) -> {
            if(menu.getShopService().createSellOrder(player, item, playerData.getQuantityInput(), playerData.getUnitPriceInput(), playerData.getCurrencyTypeInput())) {
                onClose();
                menu.getOrderMenu().getControllers().getPlayerInventoryController(player, new OrderMenuController(player, inventory, menu)).openMenu(item);
            }
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

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{3,0}, 2, 3, borderItem);
        fillRectangleArea(new int[]{1,2}, 1, 7, borderItem);
    }

    private void results() {
        if(playerData != null && playerData.getCurrencyTypeIcon() != null) {
            setItem(currencySlot, playerData.getCurrencyTypeIcon());
        } else {
            setItem(currencySlot, getUnavailableIcon());
        }
        if(playerData != null && playerData.getQuantityIcon() != null) {
            setItem(quantitySlot, playerData.getQuantityIcon());
        } else {
            setItem(quantitySlot, getUnavailableIcon());
        }
        if(playerData != null && playerData.getUnitPriceIcon() != null) {
            setItem(priceSlot, playerData.getUnitPriceIcon());
        } else {
            setItem(priceSlot, getUnavailableIcon());
        }
        if(playerData != null && playerData.getUnitPriceIcon() != null && playerData.getQuantityIcon() != null && playerData.getCurrencyTypeIcon() != null) {
            double totalProfit = playerData.getQuantityInput() * playerData.getUnitPriceInput();
            ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
            ItemMeta m = confirmButton.getItemMeta();
            if(m != null) {
                m.displayName(Component.text("CONFIRM", NamedTextColor.GREEN));
                m.lore(Arrays.asList(Component.text("TOTAL PROFIT: ", NamedTextColor.RED).append(Component.text(totalProfit, NamedTextColor.DARK_GREEN).append(playerData.getCurrencyTypeIcon().displayName()))));
            }
            confirmButton.setItemMeta(m);
            setItem(confirmOrderButton, confirmButton);
        } else {
            setItem(confirmOrderButton, getUnavailableIcon());
        }
    }

    private void orderInfo() {
        ItemStack order = new ItemStack(Material.CREEPER_BANNER_PATTERN);
        ItemMeta orderMeta = order.getItemMeta();
        if(orderMeta != null && playerData != null) {
            orderMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            orderMeta.displayName(Component.text("SELL ORDER:", NamedTextColor.GOLD));
            List<Component> components = new ArrayList<>();
            if(playerData.getCurrencyTypeIcon() != null) {
                Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(playerData.getCurrencyTypeInput(), NamedTextColor.GOLD));
                components.add(currencyComponent);
            }
            if(playerData.getQuantityIcon() != null) {
                Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(playerData.getQuantityInput() + "x", NamedTextColor.GOLD));
                components.add(quantityComponent);
            }
            if(playerData.getUnitPriceIcon() != null) {
                Component priceComponent = Component.text("Unit Price: ", NamedTextColor.BLUE).append(Component.text(playerData.getUnitPriceInput() + "$/unit", NamedTextColor.GOLD));
                components.add(priceComponent);
            }
            orderMeta.lore(components);
        }
        order.setItemMeta(orderMeta);
        setItem(orderInfo, order);
    }

    private void buttons() {
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(setCurrencyTypeButton, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Set Currency Type", NamedTextColor.WHITE)));
        setItem(setQuantityButton, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Set Quantity", NamedTextColor.WHITE)));
        setItem(setPriceButton, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Set Unit Price", NamedTextColor.WHITE)));
    }
}
