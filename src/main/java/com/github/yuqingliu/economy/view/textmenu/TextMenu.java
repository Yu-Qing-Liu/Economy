package com.github.yuqingliu.economy.view.textmenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import lombok.Getter;
import lombok.Setter;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class TextMenu extends AbstractPlayerInventory implements Listener {
    private Map<Player, AnvilView> views = new ConcurrentHashMap<>();
    private String input;
    @Setter private Consumer<String> onCloseCallback;

    public TextMenu(EventManager eventManager, SoundManager soundManager, Logger logger, Component displayName) {
        super(
            eventManager,
            soundManager,
            logger,
            displayName,
            0
        );
        eventManager.registerEvent(this);
    }

    @Override
    public void load(Player player) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        AnvilView view = (AnvilView) player.openAnvil(null, true);
        views.put(player, view);
        Inventory inventory = view.getTopInventory();
        inventory.setItem(0, item);
        inventory.setItem(1, item);
    }

    @Override
    public void open(Player player) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        AnvilView view = (AnvilView) player.openAnvil(null, true);
        views.put(player, view);
        Inventory inventory = view.getTopInventory();
        inventory.setItem(0, item);
        inventory.setItem(1, item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack result = event.getClickedInventory().getItem(0);
            String resultName = PlainTextComponentSerializer.plainText().serialize(result.displayName());
            String dName = PlainTextComponentSerializer.plainText().serialize(displayName);
            if(resultName.contains(dName)) {
                input = views.get(player).getRenameText();
                views.get(player).getTopInventory().clear();
                player.closeInventory();
                if (onCloseCallback != null) {
                    onCloseCallback.accept(input);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            Player player = (Player) event.getPlayer();
            views.get(player).getTopInventory().clear();
        }
    }
}

