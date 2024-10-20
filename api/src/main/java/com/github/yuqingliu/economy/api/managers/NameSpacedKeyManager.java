package com.github.yuqingliu.economy.api.managers;

import org.bukkit.NamespacedKey;

public interface NameSpacedKeyManager {
    NamespacedKey getShopKey();
    NamespacedKey getVendorKey();
    NamespacedKey getBankKey();
}
