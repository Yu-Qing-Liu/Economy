package com.github.yuqingliu.economy.managers;

import com.github.yuqingliu.economy.api.managers.*;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Getter
public class PluginManagerImpl implements PluginManager {
    private final CommandManager commandManager;
    private final EventManager eventManager;
    private final InventoryManager inventoryManager;
    private final NameSpacedKeyManager nameSpacedKeyManager;
    private final SoundManager soundManager;
    private final ConfigurationManager configurationManager;
}
