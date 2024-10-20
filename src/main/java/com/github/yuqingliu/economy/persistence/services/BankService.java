package com.github.yuqingliu.economy.persistence.services;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.github.yuqingliu.economy.persistence.repositories.AccountRepository;
import com.github.yuqingliu.economy.persistence.repositories.BankRepository;
import com.github.yuqingliu.economy.persistence.repositories.CurrencyRepository;
import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

@Singleton
@RequiredArgsConstructor
public class BankService {
    @Inject
    private final PlayerRepository playerRepository;
    @Inject
    private final CurrencyRepository currencyRepository;
    @Inject
    private final AccountRepository accountRepository;
    @Inject
    private final BankRepository bankRepository;

    public boolean addBankAccountToAll(String accountName, String bankName, ItemStack icon, double interestRate, String unlockCurrencyName, double unlockCost) {
        BankEntity bank = bankRepository.get(bankName);
        Set<PlayerEntity> players = playerRepository.findAll();
        for(PlayerEntity player : players) {
            AccountEntity newAccount = new AccountEntity();
            newAccount.setBank(bank);
            newAccount.setIcon(icon);
            newAccount.setPlayer(player);
            newAccount.setAccountId(player.getPlayerId());
            newAccount.setAccountName(accountName);
            newAccount.setInterestRate(interestRate);
            Set<CurrencyEntity> currencies = currencyRepository.findAllUniqueCurrencies();
            for(CurrencyEntity entity : currencies) {
                CurrencyEntity currency = new CurrencyEntity();
                currency.setCurrencyName(entity.getCurrencyName());
                currency.setAmount(0);
                currency.setIcon(entity.getIcon());
                currency.setPurseId(UUID.randomUUID());
                currency.setAccountId(player.getPlayerId());
                currency.setPurse(null);
                currency.setAccount(newAccount);
                newAccount.getCurrencies().add(currency);
            }
            if(!accountRepository.save(newAccount)) {
                return false;
            }
        }
        return true;
    }

    public boolean deleteCurrencyFromAll(String currencyName) {
        return currencyRepository.deleteAllByCurrencyName(currencyName);
    }
}
