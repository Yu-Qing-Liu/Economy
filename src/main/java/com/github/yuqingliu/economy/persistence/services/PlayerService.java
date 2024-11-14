package com.github.yuqingliu.economy.persistence.services;

import org.bukkit.OfflinePlayer;

import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerService {
    private final PlayerRepository playerRepository;

    public boolean containsPlayer(OfflinePlayer player) {
        return playerRepository.get(player.getUniqueId()) != null;
    }

    public void addPlayer(OfflinePlayer player) {
        playerRepository.addPlayer(player.getUniqueId());
    }
}

