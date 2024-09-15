package com.github.yuqingliu.economy.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.yuqingliu.economy.persistence.models.Player;
import com.github.yuqingliu.economy.persistence.services.PlayerService;

public class PlayerJoinsServer implements Listener {
    @Autowired
    private PlayerService playerService;

    @EventHandler
    public void playerJoinsServer(PlayerJoinEvent event) {
        playerService.savePlayer(event.getPlayer());
        // Retrieve and print player information
        Player player = playerService.getPlayer(event.getPlayer().getUniqueId());
        if (player != null) {
            event.getPlayer().sendMessage("Player name from DB: " + player.getId());
        } else {
            event.getPlayer().sendMessage("Player not found in the database.");
        }
    }
}
