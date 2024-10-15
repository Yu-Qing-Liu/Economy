package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Singleton
public class InventoryManagerImpl implements InventoryManager {
    private final EventManager eventManager;
    private final CurrencyService currencyService;
    private final VendorService vendorService;
    private final ShopService shopService;
    private Map<String, AbstractPlayerInventory> inventories = new HashMap<>();
    
    @Inject
    public InventoryManagerImpl(EventManager eventManager, CurrencyService currencyService, VendorService vendorService, ShopService shopService) {
        this.eventManager = eventManager;
        this.currencyService = currencyService;
        this.vendorService = vendorService;
        this.shopService = shopService;
        initializeInventories();
    }

    private void initializeInventories() {
        inventories.put(
            PurseMenu.class.getSimpleName(),
            new PurseMenu(
                eventManager,
                Component.text("Purse", NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                currencyService
            )
        );
        inventories.put(
            VendorMenu.class.getSimpleName(),
            new VendorMenu(
                eventManager,
                null,
                vendorService,
                currencyService
            )
        );
        inventories.put(
            ShopMenu.class.getSimpleName(),
            new ShopMenu(
                eventManager,
                null,
                shopService,
                currencyService
            )
        );
    }

    @Override
    public PlayerInventory getInventory(String className) {
        return inventories.get(className);
    }
}
