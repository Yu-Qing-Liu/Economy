package com.github.yuqingliu.economy.view.shopmenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.shopmenu.buyordermenu.BuyOrderMenu;
import com.github.yuqingliu.economy.view.shopmenu.itemmenu.ItemMenu;
import com.github.yuqingliu.economy.view.shopmenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderMenu;
import com.github.yuqingliu.economy.view.shopmenu.quickbuymenu.QuickBuyMenu;
import com.github.yuqingliu.economy.view.shopmenu.sellordermenu.SellOrderMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class ShopMenu extends AbstractPlayerInventory {
    protected final ShopService shopService;
    protected final CurrencyService currencyService;
    protected final InventoryManager inventoryManager;
    protected Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();

    public enum MenuType {
        MainMenu, ItemMenu, OrderMenu, BuyOrderMenu, SellOrderMenu, QuickBuyMenu, QuickSellMenu;
    }

    protected final MainMenu mainMenu;
    protected final ItemMenu itemMenu;
    protected final OrderMenu orderMenu;
    protected final BuyOrderMenu buyOrderMenu;
    protected final SellOrderMenu sellOrderMenu;
    protected final QuickBuyMenu quickBuyMenu;

    @Inject
    public ShopMenu(EventManager eventManager, Component displayName, ShopService shopService, CurrencyService currencyService, InventoryManager inventoryManager) {
        super(
            eventManager,
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
    public void load(Player player) {
        inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
    }

    @Override
    public void open(Player player) {
        inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        mainMenu.getController().openMainMenu(inventory, player);
    }
}
