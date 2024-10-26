package com.github.yuqingliu.economy.view.shopmenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.shopmenu.buyorderdetails.BuyOrderDetailsMenu;
import com.github.yuqingliu.economy.view.shopmenu.buyordermenu.BuyOrderMenu;
import com.github.yuqingliu.economy.view.shopmenu.itemmenu.ItemMenu;
import com.github.yuqingliu.economy.view.shopmenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderMenu;
import com.github.yuqingliu.economy.view.shopmenu.ordersmenu.OrdersMenu;
import com.github.yuqingliu.economy.view.shopmenu.quickbuymenu.QuickBuyMenu;
import com.github.yuqingliu.economy.view.shopmenu.quicksellmenu.QuickSellMenu;
import com.github.yuqingliu.economy.view.shopmenu.sellorderdetails.SellOrderDetailsMenu;
import com.github.yuqingliu.economy.view.shopmenu.sellordermenu.SellOrderMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class ShopMenu extends AbstractPlayerInventory {
    private final ShopService shopService;
    private final CurrencyService currencyService;
    private final InventoryManager inventoryManager;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu, ItemMenu, OrderMenu, OrdersMenu, BuyOrderMenu, SellOrderMenu, QuickBuyMenu, QuickSellMenu, BuyOrderDetailsMenu, SellOrderDetailsMenu;
    }

    private final MainMenu mainMenu;
    private final ItemMenu itemMenu;
    private final OrderMenu orderMenu;
    private final BuyOrderMenu buyOrderMenu;
    private final SellOrderMenu sellOrderMenu;
    private final QuickBuyMenu quickBuyMenu;
    private final QuickSellMenu quickSellMenu;
    private final OrdersMenu ordersMenu;
    private final BuyOrderDetailsMenu buyOrderDetailsMenu;
    private final SellOrderDetailsMenu sellOrderDetailsMenu;

    @Inject
    public ShopMenu(EventManager eventManager, SoundManager soundManager, Logger logger, Component displayName, ShopService shopService, CurrencyService currencyService, InventoryManager inventoryManager) {
        super(
            eventManager,
            soundManager,
            logger,
            displayName,
            54
        );
        this.shopService = shopService;
        this.currencyService = currencyService;
        this.inventoryManager = inventoryManager;
        this.mainMenu = new MainMenu(this);
        this.itemMenu = new ItemMenu(this);
        this.orderMenu = new OrderMenu(this);
        this.buyOrderMenu = new BuyOrderMenu(this);
        this.sellOrderMenu = new SellOrderMenu(this);
        this.quickBuyMenu = new QuickBuyMenu(this);
        this.quickSellMenu = new QuickSellMenu(this);
        this.ordersMenu = new OrdersMenu(this);
        this.buyOrderDetailsMenu = new BuyOrderDetailsMenu(this);
        this.sellOrderDetailsMenu = new SellOrderDetailsMenu(this);
    }
    
    public String getShopName() {
        return componentToString(displayName);
    }

    private String componentToString(Component component) {
        if(component == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Override
    public Inventory load(Player player) {
        Inventory inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        return inventory;
    }

    @Override
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        mainMenu.getController().openMainMenu(inventory, player);
    }
}
