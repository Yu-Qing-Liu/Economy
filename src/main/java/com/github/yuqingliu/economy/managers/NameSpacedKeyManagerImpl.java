package com.github.yuqingliu.economy.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;

import lombok.Getter;

@Getter
public class NameSpacedKeyManagerImpl implements NameSpacedKeyManager {
    private final NamespacedKey shopKey;
    private final NamespacedKey vendorKey;

    public NameSpacedKeyManagerImpl(JavaPlugin plugin) {
        shopKey = new NamespacedKey(plugin, "shopKey");
        vendorKey = new NamespacedKey(plugin, "vendorKey");
    }
}
