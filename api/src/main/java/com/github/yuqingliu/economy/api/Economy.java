package com.github.yuqingliu.economy.api;

import java.util.Objects;

import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Economy extends JavaPlugin {

    private ApplicationContext applicationContext;
    private static Economy instance;

    @Override
    public void onEnable() {
        applicationContext = new AnnotationConfigApplicationContext(SpringComponentScanConfig.class);
    }

    @Override
    public void onDisable() {
        if (applicationContext != null) {
            ((AnnotationConfigApplicationContext) applicationContext).close();
        }
    }

    @Override
    public void onLoad() {
        if(instance != null) {
            throw new RuntimeException();
        }
        instance = this;
        Scheduler.setPlugin(instance);
        Keys.load(instance);
    }
    
    @Configuration
    @ComponentScan(basePackages = {
        "com.github.yuqingliu.economy.api",
        "com.github.yuqingliu.economy"
    })
    public class SpringComponentScanConfig {
        // Optionally define beans here if needed
    }

    /**
     * Gets a main instance of Economy.
     * @return ExtraEnchants
     */
    public static Economy getInstance() {
        return Objects.requireNonNull(instance);
    }
}

