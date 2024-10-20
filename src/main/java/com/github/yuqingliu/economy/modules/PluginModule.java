package com.github.yuqingliu.economy.modules;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.*;
import com.github.yuqingliu.economy.logger.LoggerImpl;
import com.github.yuqingliu.economy.managers.*;
import com.github.yuqingliu.economy.persistence.repositories.*;
import com.github.yuqingliu.economy.persistence.services.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;

public class PluginModule extends AbstractModule {
    private final JavaPlugin plugin;

    public PluginModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Provides
    @Singleton
    public SessionFactory provideSessionFactory() {
        return HibernateModule.createSessionFactory(plugin.getDataFolder().getAbsolutePath());
    }

    @Provides
    @Singleton
    public Logger provideEconomyLogger() {
        return new LoggerImpl();
    }

    // Repositories
    @Provides
    @Singleton
    public AccountRepository provideAccountRepository(SessionFactory sessionFactory) {
        return new AccountRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public BankRepository provideBankRepository(SessionFactory sessionFactory) {
        return new BankRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public CurrencyRepository provideCurrencyRepository(SessionFactory sessionFactory) {
        return new CurrencyRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public PlayerRepository providePlayerRepository(SessionFactory sessionFactory) {
        return new PlayerRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public PurseRepository providePurseRepository(SessionFactory sessionFactory) {
        return new PurseRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public VendorRepository provideVendorRepository(SessionFactory sessionFactory) {
        return new VendorRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public VendorSectionRepository provideVendorSectionRepository(SessionFactory sessionFactory) {
        return new VendorSectionRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public VendorItemRepository provideVendorItemRepository(SessionFactory sessionFactory) {
        return new VendorItemRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public ShopRepository provideShopRepository(SessionFactory sessionFactory) {
        return new ShopRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public ShopSectionRepository provideShopSectionRepository(SessionFactory sessionFactory) {
        return new ShopSectionRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public ShopItemRepository provideShopItemRepository(SessionFactory sessionFactory) {
        return new ShopItemRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public ShopOrderRepository provideShopOrderRepository(SessionFactory sessionFactory) {
        return new ShopOrderRepository(sessionFactory);
    }

    // Services
    @Provides
    @Singleton
    public PlayerService providePlayerService(PlayerRepository playerRepository, CurrencyRepository currencyRepository) {
        return new PlayerService(playerRepository, currencyRepository);
    }

    @Provides
    @Singleton
    public CurrencyService provideCurrencyService(CurrencyRepository currencyRepository, BankRepository bankRepository, PurseRepository purseRepository) {
        return new CurrencyService(currencyRepository, bankRepository, purseRepository);
    }

    @Provides
    @Singleton
    public VendorService provideVendorService(VendorRepository vendorRepository, VendorSectionRepository vendorSectionRepository, VendorItemRepository vendorItemRepository) {
        return new VendorService(vendorRepository, vendorSectionRepository, vendorItemRepository);
    }

    @Provides
    @Singleton
    public ShopService provideShopService(ShopRepository shopRepository, ShopSectionRepository shopSectionRepository, ShopItemRepository shopItemRepository, ShopOrderRepository shopOrderRepository, CurrencyService currencyService) {
        return new ShopService(shopRepository, shopSectionRepository, shopItemRepository, shopOrderRepository, currencyService);
    }

    // Managers
    @Provides
    @Singleton
    public NameSpacedKeyManager provideNameSpacedKeyManager() {
        return new NameSpacedKeyManagerImpl(plugin);
    }

    @Provides
    @Singleton
    public SoundManager provideSoundManager() {
        return new SoundManagerImpl();
    }

    @Provides
    @Singleton
    public EventManager provideEventManager(
        PlayerService playerService,
        NameSpacedKeyManager nameSpacedKeyManager,
        InventoryManager inventoryManager
    ) {
        return new EventManagerImpl(
            plugin,
            playerService,
            nameSpacedKeyManager,
            inventoryManager
        );
    }

    @Provides
    @Singleton
    public InventoryManager provideInventoryManager(
        EventManager eventManager,
        SoundManager soundManager,
        Logger logger,
        CurrencyService currencyService,
        VendorService vendorService,
        ShopService shopService
    ) {
        return new InventoryManagerImpl(
            eventManager,
            soundManager,
            logger,
            currencyService,
            vendorService,
            shopService
        );
    }

    @Provides
    @Singleton
    public CommandManager provideCommandManager(
        Logger logger,
        InventoryManager inventoryManager,
        CurrencyService currencyService,
        VendorService vendorService,
        ShopService shopService,
        NameSpacedKeyManager nameSpacedKeyManager
    ) {
        return new CommandManagerImpl(
            plugin,
            logger,
            inventoryManager,
            currencyService,
            vendorService,
            shopService,
            nameSpacedKeyManager
        );
    }

}

