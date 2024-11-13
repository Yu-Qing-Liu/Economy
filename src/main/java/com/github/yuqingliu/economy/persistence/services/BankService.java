package com.github.yuqingliu.economy.persistence.services;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.repositories.AccountRepository;
import com.github.yuqingliu.economy.persistence.repositories.BankRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankService {
    private final AccountRepository accountRepository;
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
        return bankRepository.addBankAccountToAll(accountName, bankName, icon, interestRate, unlockCurrencyName, unlockCost);
    }
    
    public boolean deleteBankAccountFromAll(String accountName, String bankName) {
        return accountRepository.deleteBankAccountsByAccountName(accountName, bankName);
    }
    
    public List<AccountEntity> getPlayerAccountsByBank(String bankName, OfflinePlayer player) {
        return accountRepository.getPlayerAccountsByBank(bankName, player.getUniqueId());
    }
    
    public boolean depositPlayerAccount(AccountEntity account, OfflinePlayer player, double amount, String currencyName) {
        return accountRepository.depositPlayerAccount(account.getAccountId(), player.getUniqueId(), amount, currencyName);
    }

    public boolean withdrawPlayerAccount(AccountEntity account, OfflinePlayer player, double amount, String currencyName) {
        return accountRepository.withdrawPlayerAccount(account.getAccountId(), player.getUniqueId(), amount, currencyName);
    }

    public boolean depositAllInterest() {
        return bankRepository.depositAllInterestForAllBanks();
    }

    public boolean unlockAccount(AccountEntity account, OfflinePlayer player) {
        return bankRepository.unlockAccount(player.getUniqueId(), account.getAccountId());
    }
}
