package com.github.yuqingliu.economy.persistence.repositories;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.github.yuqingliu.economy.modules.Hibernate;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankRepository {
    private final Hibernate hibernate;
    private final Logger logger;
    private final SoundManager soundManager;
    
    // Transactions
    public boolean save(BankEntity bank) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            session.persist(bank);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean delete(String bankName) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            BankEntity bank = session.get(BankEntity.class, bankName);
            if (bank != null) {
                session.remove(bank);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean addBankAccountToAll(String accountName, String bankName, ItemStack icon, double interestRate, String unlockCurrencyName, double unlockCost) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            BankEntity bank = this.get(bankName);
            Set<PlayerEntity> players = Set.copyOf(session.createQuery("from PlayerEntity", PlayerEntity.class).list());
            for (PlayerEntity player : players) {
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
                for (CurrencyEntity entity : currencies) {
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
                session.persist(newAccount);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean unlockAccount(AccountEntity account, Player player) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            if (account.isUnlocked()) {
                return true;
            }
            double unlockPrice = account.getUnlockCost();
            String currencyType = account.getUnlockCurrencyType();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(purseCurrency.getAmount() < unlockPrice) {
                logger.sendPlayerErrorMessage(player, "Not enouch currency to unlock this bank account.");
                throw new IllegalArgumentException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - unlockPrice);
            session.merge(purseCurrency);
            account.setUnlocked(true);
            session.merge(account);
            transaction.commit();
            logger.sendPlayerNotificationMessage(player, String.format("Sucessfully unlocked account for %.2f %s", account.getUnlockCost(), account.getUnlockCurrencyType()));
            soundManager.playTransactionSound(player);
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    // Queries
    public BankEntity get(String bankName) {
        try (Session session = hibernate.getSession()) {
            return session.get(BankEntity.class, bankName);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<BankEntity> findAll() {
        try (Session session = hibernate.getSession()) {
            return Set.copyOf(session.createQuery("from BankEntity", BankEntity.class).list());
        } catch (Exception e) {
            return Set.of();
        }
    }
}
