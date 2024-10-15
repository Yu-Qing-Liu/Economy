package com.github.yuqingliu.economy.view.textmenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Getter;
import lombok.Setter;

import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.view.AbstractPlayerInventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Getter
public class TextMenu extends AbstractPlayerInventory implements Listener {
    @Setter private String[] input;
    @Setter private Runnable onCloseCallback;

    public TextMenu(EventManager eventManager, Component displayName) {
        super(
            eventManager,
            displayName,
            0
        );
        eventManager.registerEvent(this);
    }

    @Override
    public void load(Player player) {
        inventory = Bukkit.createInventory(null, InventoryType.ANVIL, displayName);
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Type here"));
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        inventory.setItem(0, item);
        inventory.setItem(1, item);
        player.openInventory(inventory);
    }

    @Override
    public void open(Player player) {
        inventory = Bukkit.createInventory(null, InventoryType.ANVIL, displayName);
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Type here"));
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        inventory.setItem(0, item);
        inventory.setItem(1, item);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!event.getView().title().equals(displayName)) {
            return;
        }
        ItemStack item = event.getInventory().getItem(0);
        if (item != null && item.hasItemMeta()) {
            input[0] = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
            ItemStack result = new ItemStack(Material.PAPER);
            ItemMeta resultMeta = result.getItemMeta();
            resultMeta.displayName(item.getItemMeta().displayName());
            result.setItemMeta(resultMeta);
            event.setResult(result);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL && event.getView().title().equals(displayName)) {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        }
    }
}

