package com.github.yuqingliu.economy.view.shopmenu.buyordermenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class BuyOrderMenuController {
    private final ShopMenu shopMenu;
    protected final int prev = 11;
    protected final int exit = 15;
    protected final int itemSlot = 13;
    protected final int setCurrencyType = 28;
    protected final int setQuantity = 30;
    protected final int setUnitPrice = 32;
    protected final int confirmButton = 34;
    protected final int currency = 37;
    protected final int quantity = 39;
    protected final int unitPrice = 41;
    protected final int result = 43;
    protected Material voidOption = Material.GLASS_PANE;
    protected final List<Integer> buttons = Arrays.asList(11,13,15,28,30,32,34);
    protected final List<Integer> results = Arrays.asList(37,39,41,43);
    protected final List<Integer> border = Arrays.asList(3,4,5,12,14,19,20,21,22,23,24,25);
    protected ShopItemEntity item;
    protected Player player;
    protected String currencyTypeInput;
    protected String quantityInput;
    protected String unitPriceInput;
    
    public BuyOrderMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }

    public void openBuyOrderMenu(Inventory inv, ShopItemEntity item, Player player) {
        this.item = item;
        this.player = player;
        Scheduler.runLaterAsync((task) -> {
            shopMenu.setCurrentMenu(MenuType.BuyOrderMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
        frame(inv);
        buttons(inv);
        results(inv);
        border(inv);
        displayItem(inv);
    }

    public void setCurrencyType(Inventory inv) {
        inv.close();
        PlayerInventory shop = shopMenu.getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));

        Consumer<String> callback = (currencyName) -> {
            shop.load(player);
            shopMenu.getBuyOrderMenu().getController().openBuyOrderMenu(shop.getInventory(), item, player);
            Scheduler.runAsync((task) -> {
                CurrencyEntity curr = shopMenu.getCurrencyService().getCurrencyByName(currencyName);
                if (curr != null) {
                    currencyTypeInput = currencyName;
                    shop.getInventory().setItem(currency, curr.getIcon().clone());
                }
            });
        };        

        TextMenu scanner = (TextMenu) shopMenu.getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("currency", NamedTextColor.RED));
        scanner.open(player);
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

    private void results(Inventory inv) {
        ItemStack placeholder = new ItemStack(voidOption);
        ItemMeta meta = placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        placeholder.setItemMeta(meta);
        for(int i : results) {
            inv.setItem(i, placeholder);
        }
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

        ItemStack confirm = new ItemStack(Material.CREEPER_BANNER_PATTERN);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if(confirmMeta != null) {
            confirmMeta.displayName(Component.text("Confirm", NamedTextColor.GOLD));
        }
        confirm.setItemMeta(confirmMeta);
        inv.setItem(this.confirmButton, confirm);
    }
}