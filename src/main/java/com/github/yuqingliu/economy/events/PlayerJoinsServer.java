package com.github.yuqingliu.economy.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinsServer implements Listener {
    @EventHandler
    public void playerJoinsServer(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Hello");
    }
}
