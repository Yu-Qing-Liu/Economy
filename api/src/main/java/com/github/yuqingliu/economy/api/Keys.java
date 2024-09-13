package com.github.yuqingliu.economy.api;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * A basic registry to expose {@link NamespacedKey}.
 */
public final class Keys {
    private Keys() {}
    
    @Getter private static NamespacedKey shop;

    /**
     * Used by the Main on load.
     * @param plugin the namespace to use for the namespaced-keys.
     */
    public static void load(Plugin plugin) {
        shop = new NamespacedKey(plugin, "shop");
    }
}

