package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.yuqingliu.economy.api.managers.CommandManager;
import com.github.yuqingliu.economy.commands.*;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.google.inject.Inject;

public class CommandManagerImpl implements CommandManager {
    private final JavaPlugin plugin;
    private final CurrencyService currencyService;
    private Map<String, CommandExecutor> commands = new HashMap<>();
        
    @Inject
    public CommandManagerImpl(JavaPlugin plugin, CurrencyService currencyService) {
        this.plugin = plugin;
        this.currencyService = currencyService;
        initializeCommands();
        registerCommands();
    }

    private void initializeCommands() {
        // Currency commands
        commands.put("currency", new Currency(currencyService));
    }

    private void registerCommands() {
        for(Map.Entry<String, CommandExecutor> entry : commands.entrySet()) {
            String name = entry.getKey();
            CommandExecutor command = entry.getValue();
            registerCommand(name, command);
        }
    }

    @Override
    public CommandExecutor getCommand(String name) {
        return commands.get(name);
    }

    @Override
    public void registerCommand(String name, CommandExecutor command) {
        plugin.getCommand(name).setExecutor(command);
    }
}
