package com.github.yuqingliu.economy.modules;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.managers.EventManagerImpl;
import com.google.inject.AbstractModule;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginModule extends AbstractModule {
    private final JavaPlugin plugin;

    public PluginModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(EventManager.class).toInstance(new EventManagerImpl(plugin));
    }
}
