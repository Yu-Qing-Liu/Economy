package com.github.yuqingliu.economy.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@RequiredArgsConstructor
public class PlayerOpensBank implements Listener {
    @Inject
    private final NameSpacedKeyManager nameSpacedKeyManager;
    @Inject
    private final InventoryManager inventoryManager;

    @EventHandler
    public void playerOpensBank(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if(entity.getType() == EntityType.VILLAGER) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            if(container.has(nameSpacedKeyManager.getBankKey())) {
                String bankName = container.get(nameSpacedKeyManager.getBankKey(), PersistentDataType.STRING);
                Component bankComponentName = Component.text(bankName, NamedTextColor.DARK_GRAY);
                PlayerInventory inventory = inventoryManager.getInventory(BankMenu.class.getSimpleName());
                inventory.setDisplayName(bankComponentName);
                inventory.open(event.getPlayer());
            }
        }
    }
}
