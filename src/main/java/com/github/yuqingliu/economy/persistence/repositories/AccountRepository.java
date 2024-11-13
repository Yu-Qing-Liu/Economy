package com.github.yuqingliu.economy.persistence.repositories;

import org.bukkit.OfflinePlayer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AccountRepository {
    private final SessionFactory sessionFactory;

    // Transactions 
    public boolean deleteBankAccountsByAccountName(String accountName, String bankName) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            BankEntity bank = session.get(BankEntity.class, bankName);
            if (bank == null) {
                return false;
            }
            Query<AccountEntity> query = session.createQuery(
                "FROM AccountEntity a WHERE a.accountName = :accountName AND a.bank.bankName = :bankName", 
                AccountEntity.class
            );
            query.setParameter("accountName", accountName);
            query.setParameter("bankName", bankName);
            List<AccountEntity> accountsToDelete = query.list();
            for (AccountEntity account : accountsToDelete) {
                PlayerEntity player = session.get(PlayerEntity.class, account.getAccountId());
                bank.getAccounts().remove(account);
                player.getAccounts().remove(account);
                session.remove(account);
                session.persist(bank);
                session.persist(player);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean depositPlayerAccount(UUID accountId, UUID playerId, double amount, String currencyName) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Query<CurrencyEntity> query1 = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                CurrencyEntity.class
            );
            query1.setParameter("purseId", playerId);
            query1.setParameter("currencyName", currencyName);
            Query<CurrencyEntity> query2 = session.createQuery(
                "FROM CurrencyEntity c WHERE c.accountId = :accountId AND c.currencyName = :currencyName",
                CurrencyEntity.class
            );
            query2.setParameter("accountId", accountId);
            query2.setParameter("currencyName", currencyName);
            CurrencyEntity purseCurrency = query1.uniqueResult();
            CurrencyEntity accountCurrency = query2.uniqueResult();
            
            if(purseCurrency.getAmount() < amount) {
                throw new IllegalArgumentException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - amount);
            accountCurrency.setAmount(accountCurrency.getAmount() + amount);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean delete(UUID accountId) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            AccountEntity account = session.get(AccountEntity.class, accountId);
            if (account != null) {
                session.remove(account);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    // Queries
    public AccountEntity get(UUID accountId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(AccountEntity.class, accountId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<AccountEntity> getPlayerAccountsByBank(String bankName, UUID playerId) {
        try (Session session = sessionFactory.openSession()) {
            BankEntity bank = session.get(BankEntity.class, bankName);
            if (bank == null) {
                return Collections.emptyList();
            }
            Query<AccountEntity> query = session.createQuery("FROM AccountEntity a WHERE a.player.playerId = :playerId AND a.bank.bankName = :bankName", AccountEntity.class);
            query.setParameter("playerId", playerId);
            query.setParameter("bankName", bankName);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Set<AccountEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery("from AccountEntity", AccountEntity.class).list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
