package com.github.yuqingliu.economy.persistence.services;

import org.bukkit.OfflinePlayer;

import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
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
        if(!containsPlayer(player)) {
            PlayerEntity playerEntity = new PlayerEntity();
            playerEntity.setPlayerId(player.getUniqueId());
            PurseEntity playerPurse = new PurseEntity();
            playerPurse.setPlayerId(player.getUniqueId());
            playerEntity.setPurse(playerPurse);
            playerRepository.save(playerEntity);
        }
    }

    public void deletePlayer(OfflinePlayer player) {
        playerRepository.delete(player.getUniqueId());
    }
}

