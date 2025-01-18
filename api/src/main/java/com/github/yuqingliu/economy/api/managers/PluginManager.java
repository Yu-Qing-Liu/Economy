package com.github.yuqingliu.economy.api.managers;

public interface PluginManager {
    CommandManager getCommandManager();
    EventManager getEventManager();
    InventoryManager getInventoryManager();
    NameSpacedKeyManager getNameSpacedKeyManager();
    SoundManager getSoundManager();
    ConfigurationManager getConfigurationManager();
}
