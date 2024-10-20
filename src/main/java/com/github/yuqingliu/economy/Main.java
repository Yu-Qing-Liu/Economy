package com.github.yuqingliu.economy;

import com.github.yuqingliu.economy.api.Economy;
import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.CommandManager;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.github.yuqingliu.economy.modules.PluginModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import lombok.Getter;

@Getter
public class Main extends Economy {
    private Injector injector;

    @Inject
    private Logger economyLogger;
    @Inject
    private EventManager eventManager;
    @Inject
    private CommandManager commandManager;
    @Inject
    private InventoryManager inventoryManager;
    @Inject
    private NameSpacedKeyManager nameSpacedKeyManager;

    @Override
    public void onEnable() {
        // Initialize Guice
        injector = Guice.createInjector(new PluginModule(this));
        // Inject dependencies
        injector.injectMembers(this);
    }

    @Override
    public void onDisable() {

    }
}

