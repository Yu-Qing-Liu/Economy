package com.github.yuqingliu.economy.persistence.services;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class PlayerService {
    @Inject
    private final PlayerRepository playerRepository;

    public boolean containsPlayer(OfflinePlayer player) {
        return playerRepository.get(player.getUniqueId()) != null;
    }

    public void addPlayer(OfflinePlayer player) {
        PlayerEntity playerEntity = new PlayerEntity(player.getUniqueId());
        if(!containsPlayer(player)) {
            playerRepository.save(playerEntity);
        }
    }

    public void deletePlayer(OfflinePlayer player) {
        playerRepository.delete(player.getUniqueId());
    }
}

