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
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.buyordermenu.PlayerData;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class SellOrderMenuController {
    private final ShopMenu shopMenu;
    protected final int prev = 11;
    protected final int exit = 15;
    protected final int itemSlot = 13;
    protected final int setCurrencyType = 28;
    protected final int setQuantity = 30;
    protected final int setUnitPrice = 32;
    protected final int orderButton = 34;
    protected final int currency = 37;
    protected final int quantity = 39;
    protected final int unitPrice = 41;
    protected final int confirm = 43;
    protected Material voidOption = Material.GLASS_PANE;
    protected final List<Integer> buttons = Arrays.asList(11,13,15,28,30,32,34);
    protected final List<Integer> results = Arrays.asList(37,39,41,43);
    protected final List<Integer> border = Arrays.asList(3,4,5,12,14,19,20,21,22,23,24,25);
    protected ShopItemEntity item;
    protected Map<Player, PlayerData> playersData = new ConcurrentHashMap<>();
    protected BukkitTask refreshTask;
    
    public SellOrderMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }

    public void openBuyOrderMenu(Inventory inv, ShopItemEntity item, Player player) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.BuyOrderMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
        frame(inv);
        buttons(inv);
        refreshTask = Scheduler.runTimerAsync((task) -> {
            orderButton(inv, player);
            results(inv, player);
        }, Duration.ofSeconds(1), Duration.ofSeconds(0));
        border(inv);
        displayItem(inv);
    }

    public void setCurrencyType(Inventory inv, Player player) {
        inv.close();
        PlayerInventory shop = shopMenu.getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            shop.load(player);
            shopMenu.getBuyOrderMenu().getController().openBuyOrderMenu(shop.getInventory(), item, player);
            Scheduler.runAsync((task) -> {
                CurrencyEntity curr = shopMenu.getCurrencyService().getCurrencyByName(userInput);
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
                    shop.getInventory().setItem(currency, icon);
                }
            });
        };        

        TextMenu scanner = (TextMenu) shopMenu.getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("currency", NamedTextColor.RED));
        scanner.open(player);
    }

    public void setQuantity(Inventory inv, Player player) {
        inv.close();
        PlayerInventory shop = shopMenu.getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            shop.load(player);
            shopMenu.getBuyOrderMenu().getController().openBuyOrderMenu(shop.getInventory(), item, player);
            try {
                int quantityInput = Integer.parseInt(userInput);
                ItemStack quantityIcon = new ItemStack(Material.PAPER);
                ItemMeta meta = quantityIcon.getItemMeta();
                if(meta != null) {
                    meta.displayName(Component.text(String.format("%sx", userInput), NamedTextColor.DARK_GREEN));
                }
                quantityIcon.setItemMeta(meta);
                shop.getInventory().setItem(quantity, quantityIcon);
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

        TextMenu scanner = (TextMenu) shopMenu.getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("quantity", NamedTextColor.RED));
        scanner.open(player);
    }

    public void setUnitPrice(Inventory inv, Player player) {
        inv.close();
        PlayerInventory shop = shopMenu.getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (userInput) -> {
            shop.load(player);
            shopMenu.getBuyOrderMenu().getController().openBuyOrderMenu(shop.getInventory(), item, player);
            try {
                double unitPriceInput = Double.parseDouble(userInput);
                ItemStack unitPriceIcon = new ItemStack(Material.PAPER);
                ItemMeta meta = unitPriceIcon.getItemMeta();
                if(meta != null) {
                    meta.displayName(Component.text(String.format("%s $/unit", userInput), NamedTextColor.DARK_GREEN));
                }
                unitPriceIcon.setItemMeta(meta);
                shop.getInventory().setItem(unitPrice, unitPriceIcon);
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

        TextMenu scanner = (TextMenu) shopMenu.getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("unit price", NamedTextColor.RED));
        scanner.open(player);
    }

    public void confirmOrder(Player player) {
        PlayerData data = playersData.get(player);
        Scheduler.runAsync((task) -> {
            shopMenu.getShopService().createBuyOrder(player, item, data.getQuantityInput(), data.getUnitPriceInput(), data.getCurrencyTypeInput());
        });
    }

    private void displayItem(Inventory inv) {
        inv.setItem(itemSlot, item.getIcon().clone());
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

    private void results(Inventory inv, Player player) {
        PlayerData data = playersData.get(player);
        ItemStack placeholder = new ItemStack(voidOption);
        ItemMeta meta = placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        placeholder.setItemMeta(meta);
        for(int i : results) {
            switch (i) {
                case currency:
                    if(data.getCurrencyTypeIcon() != null) {
                        inv.setItem(i, data.getCurrencyTypeIcon());
                    } else {
                        inv.setItem(i, placeholder);
                    }
                    break;
                case quantity:
                    if(data.getQuantityIcon() != null) {
                        inv.setItem(i, data.getQuantityIcon());
                    } else {
                        inv.setItem(i, placeholder);
                    }
                    break;
                case unitPrice: 
                    if(data.getUnitPriceIcon() != null) {
                        inv.setItem(i, data.getUnitPriceIcon());
                    } else {
                        inv.setItem(i, placeholder);
                    }
                    break;
                case confirm:
                    if(data.getUnitPriceIcon() != null && data.getQuantityIcon() != null && data.getCurrencyTypeIcon() != null) {
                        double totalProfit = data.getQuantityInput() * data.getUnitPriceInput();
                        ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
                        ItemMeta m = confirmButton.getItemMeta();
                        if(m != null) {
                            m.displayName(Component.text("CONFIRM", NamedTextColor.GREEN));
                            m.lore(Arrays.asList(Component.text("TOTAL PROFIT: ", NamedTextColor.RED).append(Component.text(totalProfit, NamedTextColor.DARK_GREEN).append(data.getCurrencyTypeIcon().displayName()))));
                        }
                        confirmButton.setItemMeta(m);
                        inv.setItem(i, confirmButton);
                    } else {
                        inv.setItem(i, placeholder);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void orderButton(Inventory inv, Player player) {
        PlayerData data = playersData.get(player);
        ItemStack order = new ItemStack(Material.CREEPER_BANNER_PATTERN);
        ItemMeta orderMeta = order.getItemMeta();
        if(orderMeta != null) {
            orderMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            orderMeta.displayName(Component.text("SELL ORDER:", NamedTextColor.GOLD));
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
        inv.setItem(this.orderButton, order);
    }

    private void buttons(Inventory inv) {
        ItemStack prev = new ItemStack(Material.GREEN_WOOL);
        ItemMeta prevmeta = prev.getItemMeta();
        if(prevmeta != null) {
            prevmeta.displayName(Component.text("Items", NamedTextColor.GRAY));
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

        ItemStack currencyType = new ItemStack(Material.OAK_HANGING_SIGN);
        ItemMeta currencyTypeMeta = currencyType.getItemMeta();
        if(currencyTypeMeta != null) {
            currencyTypeMeta.displayName(Component.text("Set Currency Type", NamedTextColor.DARK_PURPLE));
        }
        currencyType.setItemMeta(currencyTypeMeta);
        inv.setItem(this.setCurrencyType, currencyType);

        ItemStack quantity = new ItemStack(Material.OAK_HANGING_SIGN);
        ItemMeta quantityMeta = quantity.getItemMeta();
        if(quantityMeta != null) {
            quantityMeta.displayName(Component.text("Set Quantity", NamedTextColor.DARK_PURPLE));
        }
        quantity.setItemMeta(quantityMeta);
        inv.setItem(this.setQuantity, quantity);

        ItemStack unitPrice = new ItemStack(Material.OAK_HANGING_SIGN);
        ItemMeta unitPriceMeta = unitPrice.getItemMeta();
        if(unitPriceMeta != null) {
            unitPriceMeta.displayName(Component.text("Set Price Per Unit", NamedTextColor.DARK_PURPLE));
        }
        unitPrice.setItemMeta(unitPriceMeta);
        inv.setItem(this.setUnitPrice, unitPrice);
    }
}

