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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
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

    public boolean addBank(String bankName, long cooldown) {
        BankEntity newBank = new BankEntity();
        newBank.setBankName(bankName);
        newBank.setInterestCooldown(cooldown);
        newBank.setLastInterestTimestamp(Instant.now());
        return bankRepository.save(newBank);
    }

    public boolean deleteBank(String bankName) {
        return bankRepository.delete(bankName);
    }

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
            Set<CurrencyEntity> currencies = player.getPurse().getCurrencies();
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

    public boolean deleteBankAccountFromAll(String accountName, String bankName) {
        return accountRepository.deleteBankAccountsByAccountName(accountName, bankName);
    }

    public List<AccountEntity> getPlayerAccountsByBank(String bankName, OfflinePlayer player) {
        return accountRepository.getPlayerAccountsByBank(bankName, player.getUniqueId());
    }
}
