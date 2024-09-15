package com.github.yuqingliu.economy.persistence.services;

import com.github.yuqingliu.economy.persistence.models.Player;
import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;

import org.bukkit.OfflinePlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlayerService {
    
    @Autowired
    private PlayerRepository playerRepository;

    public Player fromBukkitPlayer(OfflinePlayer bukkitPlayer) {
        return new Player(bukkitPlayer.getUniqueId());
    }

    public void savePlayer(OfflinePlayer bukkitPlayer) {
        Player player = fromBukkitPlayer(bukkitPlayer);
        playerRepository.save(player);
    }

    public Player getPlayer(UUID id) {
        return playerRepository.findById(id);
    }

    public void deletePlayer(UUID id) {
        playerRepository.deleteById(id);
    }
}

