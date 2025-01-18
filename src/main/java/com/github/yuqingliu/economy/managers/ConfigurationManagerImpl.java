package com.github.yuqingliu.economy.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.yuqingliu.economy.api.managers.ConfigurationManager;
import com.google.inject.Inject;

import lombok.Getter;

@Getter
public class ConfigurationManagerImpl implements ConfigurationManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    
    @Inject
    public ConfigurationManagerImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Inject
    public void postConstruct() {
        plugin.saveConfig();
    }

    @Override
    public <T> T setConstant(String key, T value, Class<T> clazz) {
        if(!config.isSet(key)) {
            config.set(key, value);
            return value;
        }
        return clazz.cast(config.get(key));
    }
}
