package com.github.yuqingliu.economy.view.bankmenu;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.inject.Inject;

import lombok.Getter;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.persistence.services.BankService;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.bankmenu.accountmenu.AccountMenu;
import com.github.yuqingliu.economy.view.bankmenu.depositmenu.DepositMenu;
import com.github.yuqingliu.economy.view.bankmenu.mainmenu.MainMenu;
import com.github.yuqingliu.economy.view.bankmenu.mainmenu.MainMenuController;
import com.github.yuqingliu.economy.view.bankmenu.withdrawmenu.WithdrawMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class BankMenu extends AbstractPlayerInventory {
    private final BankService bankService;
    private final CurrencyService currencyService;
    private Map<Player, MenuType> playerMenuTypes = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public enum MenuType {
        MainMenu, AccountMenu, DepositMenu, WithdrawMenu;
    }

    private final MainMenu mainMenu;
    private final AccountMenu accountMenu;
    private final DepositMenu depositMenu;
    private final WithdrawMenu WithdrawMenu;

    @Inject
    public BankMenu(PluginManager pluginManager, Logger logger, Component displayName, BankService bankService, CurrencyService currencyService) {
        super(
            pluginManager,
            logger,
            displayName,
            27
        );
        this.bankService = bankService;
        this.currencyService = currencyService;
        this.mainMenu = new MainMenu(this);
        this.accountMenu = new AccountMenu(this);
        this.depositMenu = new DepositMenu(this);
        this.WithdrawMenu = new WithdrawMenu(this);
        interestTask();
    }
    
    public String getBankName() {
        return componentToString(displayName);
    }

    private String componentToString(Component component) {
        if(component == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private void interestTask() {
        Scheduler.runTimerAsync((task) -> {
            synchronized (lock) {
                try {
                    bankService.depositAllInterest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, Duration.ofSeconds(10) , Duration.ofSeconds(0));
    }

    @Override
    public Inventory load(Player player) {
        Inventory inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        return inventory;
    }

    @Override
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, inventorySize, displayName);
        player.openInventory(inventory);
        mainMenu.getControllers().getPlayerInventoryController(player, new MainMenuController(player, inventory, this)).openMenu();
    }
}
