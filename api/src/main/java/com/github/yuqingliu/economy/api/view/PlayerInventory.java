package com.github.yuqingliu.economy.api.view;

import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public interface PlayerInventory {
    void open(Player player);
    void load(Player player);
    void setDisplayName(Component displayName);
}
