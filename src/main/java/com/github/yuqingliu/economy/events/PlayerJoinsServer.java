package com.github.yuqingliu.economy.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.yuqingliu.economy.persistence.services.PlayerService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerJoinsServer implements Listener {
    @Inject
    private final PlayerService playerService;

    @EventHandler
    public void playerJoinsServer(PlayerJoinEvent event) {
        playerService.addPlayer(event.getPlayer());
    }
}
