package com.github.yuqingliu.economy.persistence.repositories;

import com.github.yuqingliu.economy.persistence.models.Player;

import java.util.UUID;

public interface PlayerRepository {
    void save(Player player);
    Player findById(UUID id);
    void deleteById(UUID id);
}

