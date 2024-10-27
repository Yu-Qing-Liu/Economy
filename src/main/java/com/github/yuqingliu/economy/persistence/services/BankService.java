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

import java.time.Duration;
import java.time.Instant;
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
    @Inject
    private final CurrencyService currencyService;

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
            newAccount.setUnlockCurrencyType(unlockCurrencyName);
            newAccount.setUnlockCost(unlockCost);
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

    public boolean updateAccount(AccountEntity account) {
        return accountRepository.update(account);
    }

    public boolean depositPlayerAccount(OfflinePlayer player, double amount, CurrencyEntity currency) {
        boolean sucessfulWithdrawal = currencyService.withdrawPlayerPurse(player, currency.getCurrencyName(), amount);
        if(!sucessfulWithdrawal) {
            return false;
        }
        double initial = currency.getAmount();
        currency.setAmount(initial + amount);
        boolean sucessfulDeposit = currencyRepository.update(currency);
        if(!sucessfulDeposit) {
            currency.setAmount(initial);
            currencyService.depositPlayerPurse(player, currency.getCurrencyName(), amount);
            return false;
        }
        return true;
    }

    public boolean withdrawPlayerAccount(OfflinePlayer player, double amount, CurrencyEntity currency) {
        if(currency.getAmount() < amount) {
            return false;
        }
        double initial = currency.getAmount();
        currency.setAmount(initial - amount);
        boolean sucessfulWithdrawal = currencyRepository.update(currency);
        if(!sucessfulWithdrawal) {
            currency.setAmount(initial);
            return false;
        }
        boolean sucessfulDeposit = currencyService.depositPlayerPurse(player, currency.getCurrencyName(), amount);
        if(!sucessfulDeposit) {
            currency.setAmount(initial);
            currencyRepository.update(currency);
            return false;
        }
        return true;
    }

    public boolean depositInterest(BankEntity bank) {
        Instant now = Instant.now();
        Instant lastInterestTimestamp = bank.getLastInterestTimestamp();
        Duration interestCooldown = bank.getInterestCooldown();
        Instant nextInterestTimestamp = lastInterestTimestamp.plus(interestCooldown);
        if(now.isAfter(nextInterestTimestamp)) {
            Set<AccountEntity> bankAccounts = bank.getAccounts();
            for(AccountEntity account : bankAccounts) {
                Set<CurrencyEntity> currencies = account.getCurrencies();
                for(CurrencyEntity currency : currencies) {
                    double initial = currency.getAmount();
                    double profit = initial * account.getInterestRate();
                    currency.setAmount(initial + profit);
                    currencyRepository.update(currency);
                }
            }
            bank.setLastInterestTimestamp(now);
            bankRepository.update(bank);
        }
        return true;
    }

    public boolean depositAllInterest() {
        Set<BankEntity> banks = bankRepository.findAll();
        banks.forEach(bank -> {
            depositInterest(bank);
        });
        return true;
    }
}
