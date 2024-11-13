package com.github.yuqingliu.economy.persistence.services;

import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.github.yuqingliu.economy.persistence.repositories.CurrencyRepository;
import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final CurrencyRepository currencyRepository;

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
            Set<CurrencyEntity> currencies = currencyRepository.findAllUniqueCurrencies();
            for(CurrencyEntity entity : currencies) {
                CurrencyEntity currency = new CurrencyEntity();
                currency.setCurrencyName(entity.getCurrencyName());
                currency.setAmount(0);
                currency.setIcon(entity.getIcon());
                currency.setPurseId(playerPurse.getPlayerId());
                currency.setAccountId(UUID.randomUUID());
                currency.setPurse(playerPurse);
                currency.setAccount(null);
                currencyRepository.save(currency);
            }
        }
    }

    public void deletePlayer(OfflinePlayer player) {
        playerRepository.delete(player.getUniqueId());
    }
}

