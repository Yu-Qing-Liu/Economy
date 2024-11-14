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

public class PluginModule extends AbstractModule {
    private final JavaPlugin plugin;

    public PluginModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Provides
    @Singleton
    public JavaPlugin provideJavaPlugin() {
        return this.plugin;
    }

    @Override
    protected void configure() {
        // Hibernate
        bind(Hibernate.class);
        // Logger
        bind(Logger.class).to(LoggerImpl.class).in(Singleton.class);
        // Repositories
        bind(AccountRepository.class).in(Singleton.class);
        bind(BankRepository.class).in(Singleton.class);
        bind(CurrencyRepository.class).in(Singleton.class);
        bind(PlayerRepository.class).in(Singleton.class);
        bind(PurseRepository.class).in(Singleton.class);
        bind(VendorRepository.class).in(Singleton.class);
        bind(VendorSectionRepository.class).in(Singleton.class);
        bind(VendorItemRepository.class).in(Singleton.class);
        bind(ShopRepository.class).in(Singleton.class);
        bind(ShopSectionRepository.class).in(Singleton.class);
        bind(ShopItemRepository.class).in(Singleton.class);
        bind(ShopOrderRepository.class).in(Singleton.class);
        // Services
        bind(PlayerService.class).in(Singleton.class);
        bind(CurrencyService.class).in(Singleton.class);
        bind(VendorService.class).in(Singleton.class);
        bind(ShopService.class).in(Singleton.class);
        bind(BankService.class).in(Singleton.class);
        // Managers
        bind(NameSpacedKeyManager.class).to(NameSpacedKeyManagerImpl.class).in(Singleton.class);
        bind(SoundManager.class).to(SoundManagerImpl.class).in(Singleton.class);
        bind(EventManager.class).to(EventManagerImpl.class).in(Singleton.class);
        bind(InventoryManager.class).to(InventoryManagerImpl.class).in(Singleton.class);
        bind(CommandManager.class).to(CommandManagerImpl.class).in(Singleton.class);
        bind(PluginManager.class).to(PluginManagerImpl.class).in(Singleton.class);
    }
}
