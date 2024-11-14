package com.github.yuqingliu.economy.persistence.repositories;

import java.util.Set;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.CurrencyKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CurrencyRepository {
    private final Hibernate hibernate;
    
    // Transactions
    public boolean save(CurrencyEntity currency) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            session.persist(currency);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean addCurrencyToAllAccountsAndPurses(String currencyName, double amount, ItemStack icon) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            Set<BankEntity> banks = Set.copyOf(session.createQuery("from BankEntity", BankEntity.class).list());
            banks.forEach(bank -> {
                bank.getAccounts().forEach(account -> {
                    CurrencyEntity bankCurrency = new CurrencyEntity();
                    bankCurrency.setCurrencyName(currencyName);
                    bankCurrency.setAmount(amount);
                    bankCurrency.setIcon(icon);
                    bankCurrency.setPurseId(UUID.randomUUID());
                    bankCurrency.setAccountId(account.getAccountId());
                    bankCurrency.setPurse(null);
                    bankCurrency.setAccount(account);
                    session.persist(bankCurrency);
                });
            });
            Set<PurseEntity> purses = Set.copyOf(session.createQuery("from PurseEntity", PurseEntity.class).list());
            purses.forEach(purse -> {
                CurrencyEntity purseCurrency = new CurrencyEntity();
                purseCurrency.setCurrencyName(currencyName);
                purseCurrency.setAmount(amount);
                purseCurrency.setIcon(icon);
                purseCurrency.setPurseId(purse.getPlayerId());
                purseCurrency.setAccountId(UUID.randomUUID());
                purseCurrency.setPurse(purse);
                purseCurrency.setAccount(null);
                session.persist(purseCurrency);
            });
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean delete(CurrencyKey key) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            CurrencyEntity currency = session.find(CurrencyEntity.class, key);
            PurseEntity purse = session.get(PurseEntity.class, key.getPurseId());
            AccountEntity account = session.get(AccountEntity.class, key.getAccountId());
            if (currency != null) {
                purse.getCurrencies().remove(currency);
                account.getCurrencies().remove(currency);
                session.remove(currency);
                session.persist(purse);
                session.persist(account);
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

    public boolean deleteAllByCurrencyName(String currencyName) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            String hql = "DELETE FROM CurrencyEntity c WHERE c.currencyName = :currencyName";
            MutationQuery query = session.createMutationQuery(hql);
            query.setParameter("currencyName", currencyName);
            int deletedCount = query.executeUpdate();
            transaction.commit();
            return deletedCount > 0;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean depositPlayerPurse(UUID playerId, String currencyName, double amount) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", playerId);
            query.setParameter("currencyName", currencyName);
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + amount);
            session.merge(purseCurrency);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean withdrawPlayerPurse(UUID playerId, String currencyName, double amount) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", playerId);
            query.setParameter("currencyName", currencyName);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(purseCurrency.getAmount() < amount) {
                throw new IllegalArgumentException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - amount);
            session.merge(purseCurrency);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    // Queries
    public CurrencyEntity get(CurrencyKey key) {
        try (Session session = hibernate.getSession()) {
            return session.find(CurrencyEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public CurrencyEntity getFirst(String currencyName) {
        try (Session session = hibernate.getSession()) {
            return session.createQuery("FROM CurrencyEntity WHERE currencyName = :currencyName", CurrencyEntity.class)
                          .setParameter("currencyName", currencyName)
                          .setMaxResults(1)
                          .uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Set<CurrencyEntity> findAllUniqueCurrencies() {
        try (Session session = hibernate.getSession()) {
            return Set.copyOf(session.createQuery(
                "FROM CurrencyEntity c WHERE c.currencyName IN (" +
                "SELECT DISTINCT c2.currencyName FROM CurrencyEntity c2)", 
                CurrencyEntity.class
            ).list());
        } catch (Exception e) {
            return Set.of();
        }
    }

    public Set<CurrencyEntity> findAll() {
        try (Session session = hibernate.getSession()) {
            return Set.copyOf(session.createQuery("from CurrencyEntity", CurrencyEntity.class).list());
        } catch (Exception e) {
            return Set.of();
        }
    }
}
