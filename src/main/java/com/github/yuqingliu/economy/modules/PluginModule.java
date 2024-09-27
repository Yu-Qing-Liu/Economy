package com.github.yuqingliu.economy.modules;

import com.github.yuqingliu.economy.api.managers.CommandManager;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.github.yuqingliu.economy.managers.CommandManagerImpl;
import com.github.yuqingliu.economy.managers.EventManagerImpl;
import com.github.yuqingliu.economy.managers.InventoryManagerImpl;
import com.github.yuqingliu.economy.managers.NameSpacedKeyManagerImpl;
import com.github.yuqingliu.economy.persistence.repositories.AccountRepository;
import com.github.yuqingliu.economy.persistence.repositories.BankRepository;
import com.github.yuqingliu.economy.persistence.repositories.CurrencyRepository;
import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;
import com.github.yuqingliu.economy.persistence.repositories.PurseRepository;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.PlayerService;
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

    // Services
    @Provides
    @Singleton
    public PlayerService providePlayerService(PlayerRepository playerRepository) {
        return new PlayerService(playerRepository);
    }

    @Provides
    @Singleton
    public CurrencyService provideCurrencyService(CurrencyRepository currencyRepository, BankRepository bankRepository, PurseRepository purseRepository) {
        return new CurrencyService(currencyRepository, bankRepository, purseRepository);
    }

    // Managers
    @Provides
    @Singleton
    public NameSpacedKeyManager provideNameSpacedKeyManager() {
        return new NameSpacedKeyManagerImpl(plugin);
    }

    @Provides
    @Singleton
    public EventManager provideEventManager(PlayerService playerService, NameSpacedKeyManager nameSpacedKeyManager, InventoryManager inventoryManager) {
        return new EventManagerImpl(plugin, playerService, nameSpacedKeyManager, inventoryManager);
    }

    @Provides
    @Singleton
    public InventoryManager provideInventoryManager(EventManager eventManager, CurrencyService currencyService) {
        return new InventoryManagerImpl(eventManager, currencyService);
    }

    @Provides
    @Singleton
    public CommandManager provideCommandManager(InventoryManager inventoryManager, CurrencyService currencyService, NameSpacedKeyManager nameSpacedKeyManager) {
        return new CommandManagerImpl(plugin, inventoryManager, currencyService, nameSpacedKeyManager);
    }

}

