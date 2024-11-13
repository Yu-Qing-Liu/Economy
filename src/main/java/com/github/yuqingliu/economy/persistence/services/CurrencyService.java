package com.github.yuqingliu.economy.persistence.services;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.github.yuqingliu.economy.persistence.repositories.BankRepository;
import com.github.yuqingliu.economy.persistence.repositories.CurrencyRepository;
import com.github.yuqingliu.economy.persistence.repositories.PurseRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final BankRepository bankRepository;
    private final PurseRepository purseRepository;

    public boolean addCurrencyToAll(String currencyName, double amount, ItemStack icon) {
        // Fetch all bank entities
        Set<BankEntity> banks = bankRepository.findAll();
        // Add currency to all bank accounts
        for (BankEntity bank : banks) {
            for (AccountEntity account : bank.getAccounts()) {
                CurrencyEntity bankCurrency = new CurrencyEntity();
                bankCurrency.setCurrencyName(currencyName);
                bankCurrency.setAmount(amount);
                bankCurrency.setIcon(icon);
                bankCurrency.setPurseId(UUID.randomUUID());
                bankCurrency.setAccountId(account.getAccountId());
                bankCurrency.setPurse(null);
                bankCurrency.setAccount(account);
                if(!currencyRepository.save(bankCurrency)) {
                    return false;
                }
            }
        }
        // Fetch all purse entities
        Set<PurseEntity> purses = purseRepository.findAll();
        // Add currency to all purses
        for (PurseEntity purse : purses) {
            CurrencyEntity purseCurrency = new CurrencyEntity();
            purseCurrency.setCurrencyName(currencyName);
            purseCurrency.setAmount(amount);
            purseCurrency.setIcon(icon);
            purseCurrency.setPurseId(purse.getPlayerId());
            purseCurrency.setAccountId(UUID.randomUUID());
            purseCurrency.setPurse(purse);
            purseCurrency.setAccount(null);
            if(!currencyRepository.save(purseCurrency)) {
                return false;
            }
        }
        return true;
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
        Set<CurrencyEntity> currencies = getPlayerPurseCurrencies(player);
        for(CurrencyEntity currency : currencies) {
            if(currency.getCurrencyName().equals(currencyName)) {
                currency.setAmount(currency.getAmount() + amount);
                return currencyRepository.update(currency);
            }
        }
        return false;
    }

    public boolean withdrawPlayerPurse(OfflinePlayer player, String currencyName, double amount) {
        Set<CurrencyEntity> currencies = getPlayerPurseCurrencies(player);
        for(CurrencyEntity currency : currencies) {
            if(currency.getCurrencyName().equals(currencyName)) {
                if(currency.getAmount() - amount >= 0) {
                    currency.setAmount(currency.getAmount() - amount);
                    return currencyRepository.update(currency);
                }
            }
        }
        return false;
    }
}
