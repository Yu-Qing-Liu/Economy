package com.github.yuqingliu.economy.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerJoinsServer implements Listener {
    @EventHandler
    public static void playerJoinsServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Welcome", NamedTextColor.RED));
    }
}
