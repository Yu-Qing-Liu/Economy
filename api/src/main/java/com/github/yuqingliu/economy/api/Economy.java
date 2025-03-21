package com.github.yuqingliu.economy.api;

import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Economy extends JavaPlugin {
    private static Economy instance;

    @Override
    public void onLoad() {
        if (instance != null) {
            throw new RuntimeException("Multiple instances of Economy detected!");
        }
        instance = this;
        Scheduler.setPlugin(instance);
    }

    /**
     * Gets a main instance of Economy.
     * @return Economy
     */
    public static Economy getInstance() {
        return Objects.requireNonNull(instance);
    }
}

