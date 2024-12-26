package com.github.yuqingliu.economy.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.Getter;

@Singleton
@Getter
public class NameSpacedKeyManagerImpl implements NameSpacedKeyManager {
    private final NamespacedKey shopKey;
    private final NamespacedKey vendorKey;
    private final NamespacedKey bankKey;
    private final NamespacedKey auctionKey;
    
    @Inject
    public NameSpacedKeyManagerImpl(JavaPlugin plugin) {
        shopKey = new NamespacedKey(plugin, "shopKey");
        vendorKey = new NamespacedKey(plugin, "vendorKey");
        bankKey = new NamespacedKey(plugin, "bankKey");
        auctionKey = new NamespacedKey(plugin, "auctionKey");
    }
}
