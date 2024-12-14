package com.github.yuqingliu.economy.view.shopmenu.buyordermenu;

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
import com.github.yuqingliu.economy.view.textmenu.TextMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class BuyOrderMenuController extends AbstractPlayerInventoryController<ShopMenu> {
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
    private final Map<Player, PlayerData> playersData = new ConcurrentHashMap<>();
    private final Map<Player, BukkitTask> tasks = new ConcurrentHashMap<>();
    private ShopItemEntity item;
    
    public BuyOrderMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
    }

    public void openBuyOrderMenu(ShopItemEntity item) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.BuyOrderMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayItem();
        BukkitTask refreshTask = Scheduler.runTimerAsync((task) -> {
            orderInfo();
            results();
        }, Duration.ofSeconds(1), Duration.ofSeconds(0));
        tasks.put(player, refreshTask);
    }

    public void setCurrencyType() {
        inventory.close();
        PlayerInventory shop = menu.getPluginManager().getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            Inventory inventory = shop.load(player);
            menu.getBuyOrderMenu().getControllers().getPlayerInventoryController(player, new BuyOrderMenuController(player, inventory, menu)).openBuyOrderMenu(item);
            Scheduler.runAsync((task) -> {
                CurrencyEntity curr = menu.getCurrencyService().getCurrencyByName(userInput);
                if (curr != null) {
                    ItemStack icon = curr.getIcon().clone();
                    if(playersData.containsKey(player)) {
                        playersData.get(player).setCurrencyTypeInput(userInput);
                        playersData.get(player).setCurrencyTypeIcon(icon);
                    } else {
                        PlayerData data = new PlayerData();
                        data.setCurrencyTypeIcon(icon);
                        data.setCurrencyTypeInput(userInput);
                        playersData.put(player, data);
                    }
                    setItem(currencySlot, icon);
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
            menu.getBuyOrderMenu().getControllers().getPlayerInventoryController(player, new BuyOrderMenuController(player, inventory, menu)).openBuyOrderMenu(item);
            try {
                int quantityInput = Integer.parseInt(userInput);
                ItemStack quantityIcon = new ItemStack(Material.PAPER);
                ItemMeta meta = quantityIcon.getItemMeta();
                if(meta != null) {
                    meta.displayName(Component.text(String.format("%sx", userInput), NamedTextColor.DARK_GREEN));
                }
                quantityIcon.setItemMeta(meta);
                setItem(quantitySlot, quantityIcon);
                if(playersData.containsKey(player)) {
                    playersData.get(player).setQuantityIcon(quantityIcon);
                    playersData.get(player).setQuantityInput(quantityInput);
                } else {
                    PlayerData data = new PlayerData();
                    data.setQuantityIcon(quantityIcon);
                    data.setQuantityInput(quantityInput);
                    playersData.put(player, data);
                }
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
            menu.getBuyOrderMenu().getControllers().getPlayerInventoryController(player, new BuyOrderMenuController(player, inventory, menu)).openBuyOrderMenu(item);
            try {
                double unitPriceInput = Double.parseDouble(userInput);
                ItemStack unitPriceIcon = new ItemStack(Material.PAPER);
                ItemMeta meta = unitPriceIcon.getItemMeta();
                if(meta != null) {
                    meta.displayName(Component.text(String.format("%s $/unit", userInput), NamedTextColor.DARK_GREEN));
                }
                unitPriceIcon.setItemMeta(meta);
                setItem(priceSlot, unitPriceIcon);
                if(playersData.containsKey(player)) {
                    playersData.get(player).setUnitPriceIcon(unitPriceIcon);
                    playersData.get(player).setUnitPriceInput(unitPriceInput);
                } else {
                    PlayerData data = new PlayerData();
                    data.setUnitPriceIcon(unitPriceIcon);
                    data.setUnitPriceInput(unitPriceInput);
                    playersData.put(player, data);
                }
            } catch (Exception e) {}
        };        

        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("unit price", NamedTextColor.RED));
        scanner.open(player);
    }

    public void confirmOrder() {
        PlayerData data = playersData.get(player);
        Scheduler.runAsync((task) -> {
            if(menu.getShopService().createBuyOrder(player, item, data.getQuantityInput(), data.getUnitPriceInput(), data.getCurrencyTypeInput())) {
                onClose();
                menu.getOrderMenu().getController().openOrderMenu(inv, item, player);
            }
        });
    }

    public void onClose() {
        if(tasks.containsKey(player)) {
            tasks.get(player).cancel();
            tasks.remove(player);
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
        PlayerData data = playersData.get(player);
        if(data != null && data.getCurrencyTypeIcon() != null) {
            setItem(currencySlot, data.getCurrencyTypeIcon());
        } else {
            setItem(currencySlot, getUnavailableIcon());
        }
        if(data != null && data.getQuantityIcon() != null) {
            setItem(quantitySlot, data.getQuantityIcon());
        } else {
            setItem(quantitySlot, getUnavailableIcon());
        }
        if(data != null && data.getUnitPriceIcon() != null) {
            setItem(priceSlot, data.getUnitPriceIcon());
        } else {
            setItem(priceSlot, getUnavailableIcon());
        }
        if(data != null && data.getUnitPriceIcon() != null && data.getQuantityIcon() != null && data.getCurrencyTypeIcon() != null) {
            double totalProfit = data.getQuantityInput() * data.getUnitPriceInput();
            ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
            ItemMeta m = confirmButton.getItemMeta();
            if(m != null) {
                m.displayName(Component.text("CONFIRM", NamedTextColor.GREEN));
                m.lore(Arrays.asList(Component.text("TOTAL COST: ", NamedTextColor.RED).append(Component.text(totalProfit, NamedTextColor.DARK_GREEN).append(data.getCurrencyTypeIcon().displayName()))));
            }
            confirmButton.setItemMeta(m);
            setItem(confirmOrderButton, confirmButton);
        } else {
            setItem(confirmOrderButton, getUnavailableIcon());
        }
    }

    private void orderInfo() {
        PlayerData data = playersData.get(player);
        ItemStack order = new ItemStack(Material.CREEPER_BANNER_PATTERN);
        ItemMeta orderMeta = order.getItemMeta();
        if(orderMeta != null && data != null) {
            orderMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            orderMeta.displayName(Component.text("BUY ORDER:", NamedTextColor.GOLD));
            List<Component> components = new ArrayList<>();
            if(data.getCurrencyTypeIcon() != null) {
                Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(data.getCurrencyTypeInput(), NamedTextColor.GOLD));
                components.add(currencyComponent);
            }
            if(data.getQuantityIcon() != null) {
                Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(data.getQuantityInput() + "x", NamedTextColor.GOLD));
                components.add(quantityComponent);
            }
            if(data.getUnitPriceIcon() != null) {
                Component priceComponent = Component.text("Unit Price: ", NamedTextColor.BLUE).append(Component.text(data.getUnitPriceInput() + "$/unit", NamedTextColor.GOLD));
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
