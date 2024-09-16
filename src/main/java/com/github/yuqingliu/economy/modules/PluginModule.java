package com.github.yuqingliu.economy.modules;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.managers.EventManagerImpl;
import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;
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

    @Provides
    @Singleton
    public PlayerRepository providePlayerRepository(SessionFactory sessionFactory) {
        return new PlayerRepository(sessionFactory);
    }

    @Provides
    @Singleton
    public PlayerService providePlayerService(PlayerRepository playerRepository) {
        return new PlayerService(playerRepository);
    }

    @Provides
    @Singleton
    public EventManager provideEventManager(PlayerService playerService) {
        return new EventManagerImpl(plugin, playerService);
    }
}

