package com.github.yuqingliu.economy.persistence.services;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.repositories.AccountRepository;
import com.github.yuqingliu.economy.persistence.repositories.BankRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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

    public AccountEntity getAccount(UUID accountId) {
        return accountRepository.get(accountId);
    }
    
    public boolean depositPlayerAccount(AccountEntity account, Player player, double amount, String currencyName) {
        return accountRepository.depositPlayerAccount(account, player, amount, currencyName);
    }

    public boolean withdrawPlayerAccount(AccountEntity account, Player player, double amount, String currencyName) {
        return accountRepository.withdrawPlayerAccount(account, player, amount, currencyName);
    }

    public boolean depositInterest(AccountEntity account) {
        return accountRepository.depositInterest(account);
    }

    public boolean unlockAccount(AccountEntity account, Player player) {
        return bankRepository.unlockAccount(account, player);
    }
}
