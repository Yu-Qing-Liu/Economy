package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.services.BankService;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Singleton
public class InventoryManagerImpl implements InventoryManager {
    private final EventManager eventManager;
    private final SoundManager soundManager;
    private final Logger logger;
    private final CurrencyService currencyService;
    private final VendorService vendorService;
    private final ShopService shopService;
    private final BankService bankService;
    private Map<String, AbstractPlayerInventory> inventories = new HashMap<>();
    
    @Inject
    public InventoryManagerImpl(EventManager eventManager, SoundManager soundManager, Logger logger, CurrencyService currencyService, VendorService vendorService, ShopService shopService, BankService bankService) {
        this.eventManager = eventManager;
        this.soundManager = soundManager;
        this.logger = logger;
        this.currencyService = currencyService;
        this.vendorService = vendorService;
        this.shopService = shopService;
        this.bankService = bankService;
        initializeInventories();
    }

    private void initializeInventories() {
        inventories.put(
            PurseMenu.class.getSimpleName(),
            new PurseMenu(
                eventManager,
                soundManager,
                logger,
                Component.text("Purse", NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                currencyService
            )
        );
        inventories.put(
            VendorMenu.class.getSimpleName(),
            new VendorMenu(
                eventManager,
                soundManager,
                logger,
                null,
                vendorService,
                currencyService
            )
        );
        inventories.put(
            TextMenu.class.getSimpleName(),
            new TextMenu(
                eventManager,
                soundManager,
                logger,
                null
            )
        );
        inventories.put(
            ShopMenu.class.getSimpleName(),
            new ShopMenu(
                eventManager,
                soundManager,
                logger,
                null,
                shopService,
                currencyService,
                this
            )
        );
        inventories.put(
            BankMenu.class.getSimpleName(),
            new BankMenu(
                eventManager,
                soundManager,
                logger,
                null,
                bankService,
                currencyService,
                this
            )
        );
    }

    @Override
    public PlayerInventory getInventory(String className) {
        return inventories.get(className);
    }
}
