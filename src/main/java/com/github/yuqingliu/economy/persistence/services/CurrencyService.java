package com.github.yuqingliu.economy.persistence.services;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.CurrencyKey;
import com.github.yuqingliu.economy.persistence.repositories.BankRepository;
import com.github.yuqingliu.economy.persistence.repositories.CurrencyRepository;
import com.github.yuqingliu.economy.persistence.repositories.PurseRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

@Singleton
@RequiredArgsConstructor
public class CurrencyService {
    @Inject
    private final CurrencyRepository currencyRepository;
    @Inject
    private final BankRepository bankRepository;
    @Inject
    private final PurseRepository purseRepository;

    public void addCurrencyToAll(String currencyName, double amount, ItemStack icon) {
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
                currencyRepository.save(bankCurrency);
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
            currencyRepository.save(purseCurrency);
        }
    }

    public void deleteCurrencyFromAll(String currencyName) {
        Set<CurrencyEntity> currencies = currencyRepository.findAll();
        for(CurrencyEntity currency : currencies) {
            if(currency.getCurrencyName().equals(currencyName)) {
                CurrencyKey key = new CurrencyKey(currency.getCurrencyName(), currency.getPurseId(), currency.getAccountId());
                currencyRepository.delete(key);
            }
        }
    }
}
