package com.github.yuqingliu.economy.managers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.PluginManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.services.AuctionService;
import com.github.yuqingliu.economy.persistence.services.BankService;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.pursemenu.PurseMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Singleton
public class InventoryManagerImpl implements InventoryManager {
    private final PluginManager pluginManager;
    private final Logger logger;
    private final CurrencyService currencyService;
    private final VendorService vendorService;
    private final ShopService shopService;
    private final BankService bankService;
    private final AuctionService auctionService;
    private Map<String, AbstractPlayerInventory> inventories = new HashMap<>();

    @Inject
    public InventoryManagerImpl(PluginManager pluginManager, Logger logger, CurrencyService currencyService,
            VendorService vendorService, ShopService shopService, BankService bankService, AuctionService auctionService) {
        this.pluginManager = pluginManager;
        this.logger = logger;
        this.currencyService = currencyService;
        this.vendorService = vendorService;
        this.shopService = shopService;
        this.bankService = bankService;
        this.auctionService = auctionService;
    }

    @Override
    public void postConstruct() {
        inventories.put(
            PurseMenu.class.getSimpleName(),
            new PurseMenu(
                pluginManager,
                logger,
                Component.text("Purse", NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                currencyService
            )
        );
        inventories.put(
            VendorMenu.class.getSimpleName(),
            new VendorMenu(
                pluginManager,
                logger,
                null,
                vendorService,
                currencyService
            )
        );
        inventories.put(
            TextMenu.class.getSimpleName(),
            new TextMenu(
                pluginManager,
                logger,
                null
            )
        );
        inventories.put(
            ShopMenu.class.getSimpleName(),
            new ShopMenu(
                pluginManager,
                logger,
                null,
                shopService,
                currencyService
            )
        );
        inventories.put(
            BankMenu.class.getSimpleName(),
            new BankMenu(
                pluginManager,
                logger,
                null,
                bankService,
                currencyService
            )
        );
        inventories.put(
            AuctionMenu.class.getSimpleName(),
            new AuctionMenu(
                pluginManager,
                logger,
                Component.text("Auction", NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                auctionService
            )
        );
    }

    @Override
    public PlayerInventory getInventory(String className) {
        return inventories.get(className);
    }

    @Override
    public void addItemToPlayer(Player player, ItemStack item, int quantity) {
        int required = quantity;
        int maxStackSize = item.getType().getMaxStackSize();
        while (required > 0) {
            int substractedAmount = Math.min(required, maxStackSize);
            item.setAmount(substractedAmount);
            if (!player.getInventory().addItem(item).isEmpty()) {
                Scheduler.runLater((task) -> {
                    Location location = player.getLocation();
                    player.getWorld().dropItemNaturally(location, item);
                }, Duration.ofSeconds(0));
            }
            required -= substractedAmount;
        }
    }

    @Override
    public boolean removeItemFromPlayer(Player player, ItemStack item, int quantity) {
        int totalItemCount = countItemFromPlayer(player, item);
        if (totalItemCount < quantity) {
            return false;
        }
        int remaining = quantity;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                int amount = inventoryItem.getAmount();
                if (amount >= remaining) {
                    inventoryItem.setAmount(amount - remaining);
                    return true;
                } else {
                    inventoryItem.setAmount(0);
                    remaining -= amount;
                }
            }
        }
        return false;
    }

    @Override
    public int countItemFromPlayer(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                count += inventoryItem.getAmount();
            }
        }
        return count;
    }

    @Override
    public int countAvailableInventorySpace(Player player, Material material) {
        Inventory inventory = player.getInventory();
        int maxStackSize = material.getMaxStackSize();
        int availableSpace = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                availableSpace += maxStackSize;
            } else if (item.getType() == material) {
                availableSpace += maxStackSize - item.getAmount();
            }
        }
        return availableSpace;
    }
}
