package com.github.yuqingliu.economy;

import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import com.github.yuqingliu.economy.managers.EventManagerImpl;
import com.github.yuqingliu.economy.api.managers.EventManager;

@Configuration(proxyBeanMethods=false)
@ComponentScan
public class SpringConfig {
    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin plugin) {
        SpringConfig.plugin = plugin;
    }

    @Bean
    public EventManager eventManager() {
        return new EventManagerImpl(plugin);
    }
}
