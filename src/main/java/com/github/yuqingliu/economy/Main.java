package com.github.yuqingliu.economy;

import com.github.yuqingliu.economy.api.Economy;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.modules.PluginModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.Getter;

@Getter
public class Main extends Economy {
    private Injector injector;

    private PluginManager pluginManager;

    @Override
    public void onEnable() {
        // Initialize Guice
        injector = Guice.createInjector(new PluginModule(this));
        // Inject dependencies
        injector.injectMembers(this);
        this.pluginManager = injector.getInstance(PluginManager.class);

        postConstruct();
    }

    private void postConstruct() {
        pluginManager.getInventoryManager().postConstruct();
    }
}

