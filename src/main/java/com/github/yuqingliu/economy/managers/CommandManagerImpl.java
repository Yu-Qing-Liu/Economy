package com.github.yuqingliu.economy.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.CommandManager;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.github.yuqingliu.economy.commands.*;
import com.github.yuqingliu.economy.persistence.services.BankService;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CommandManagerImpl implements CommandManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final InventoryManager inventoryManager;
    private final CurrencyService currencyService;
    private final VendorService vendorService;
    private final ShopService shopService;
    private final BankService bankService;
    private final NameSpacedKeyManager nameSpacedKeyManager;
    private Map<String, CommandExecutor> commands = new HashMap<>();
        
    @Inject
    public CommandManagerImpl(
        JavaPlugin plugin,
        Logger logger,
        InventoryManager inventoryManager,
        CurrencyService currencyService,
        VendorService vendorService,
        ShopService shopService,
        BankService bankService,
        NameSpacedKeyManager nameSpacedKeyManager
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.inventoryManager = inventoryManager;
        this.currencyService = currencyService;
        this.vendorService = vendorService;
        this.shopService = shopService;
        this.bankService = bankService;
        this.nameSpacedKeyManager = nameSpacedKeyManager;
    }
    
    @Inject
    public void postConstruct() {
        initializeCommands();
        registerCommands();
    }

    private void initializeCommands() {
        // Currency commands
        commands.put("currency", new CurrencyCommand(currencyService, logger));
        // Purse commands
        commands.put("purse", new PurseCommand(inventoryManager, logger));
        commands.put("deposit", new DepositCommand(currencyService, logger));
        commands.put("withdraw", new WithdrawCommand(currencyService, logger));
        // Vendor commands
        commands.put("vendor", new VendorCommand(nameSpacedKeyManager, vendorService, logger));
        commands.put("vendorsection", new VendorSectionCommand(vendorService, logger));
        commands.put("vendoritem", new VendorItemCommand(vendorService, currencyService, logger));
        // Shop commands
        commands.put("shop", new ShopCommand(nameSpacedKeyManager, shopService, logger));
        commands.put("shopsection", new ShopSectionCommand(shopService, logger));
        commands.put("shopitem", new ShopItemCommand(shopService, logger));
        // Bank commands
        commands.put("bank", new BankCommand(nameSpacedKeyManager, bankService, logger));
        commands.put("account", new AccountCommand(bankService, logger));
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
