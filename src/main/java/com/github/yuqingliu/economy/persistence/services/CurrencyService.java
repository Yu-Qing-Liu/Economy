package com.github.yuqingliu.economy.persistence.services;

import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.github.yuqingliu.economy.persistence.repositories.CurrencyRepository;
import com.github.yuqingliu.economy.persistence.repositories.PurseRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final PurseRepository purseRepository;

    public boolean addCurrencyToAll(String currencyName, double amount, ItemStack icon) {
        return currencyRepository.addCurrencyToAllAccountsAndPurses(currencyName, amount, icon);
    }

    public boolean deleteCurrencyFromAll(String currencyName) {
        return currencyRepository.deleteAllByCurrencyName(currencyName);
    }

    public Set<CurrencyEntity> getPlayerPurseCurrencies(OfflinePlayer player) {
        PurseEntity playerPurse = purseRepository.get(player.getUniqueId());
        return playerPurse.getCurrencies();
    }

    public CurrencyEntity getCurrencyByName(String currencyName) {
        return currencyRepository.getFirst(currencyName);
    }

    public boolean depositPlayerPurse(OfflinePlayer player, String currencyName, double amount) {
        return currencyRepository.depositPlayerPurse(player.getUniqueId(), currencyName, amount);
    }

    public boolean withdrawPlayerPurse(OfflinePlayer player, String currencyName, double amount) {
        return currencyRepository.withdrawPlayerPurse(player.getUniqueId(), currencyName, amount);
    }
}
