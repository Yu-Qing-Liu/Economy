package com.github.yuqingliu.economy.persistence.repositories;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.MutationQuery;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.CurrencyKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class CurrencyRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public boolean save(CurrencyEntity currency) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(currency);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(CurrencyEntity currency) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(currency);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public CurrencyEntity get(CurrencyKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(CurrencyEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }


    public CurrencyEntity getFirst(String currencyName) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM CurrencyEntity WHERE currencyName = :currencyName", CurrencyEntity.class)
                          .setParameter("currencyName", currencyName)
                          .setMaxResults(1)
                          .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(CurrencyKey key) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
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
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAllByCurrencyName(String currencyName) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            String hql = "DELETE FROM CurrencyEntity c WHERE c.currencyName = :currencyName";
            MutationQuery query = session.createMutationQuery(hql);
            query.setParameter("currencyName", currencyName);
            int deletedCount = query.executeUpdate();

            transaction.commit();
            return deletedCount > 0;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public Set<CurrencyEntity> findAllUniqueCurrencies() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery(
                    "FROM CurrencyEntity c WHERE c.currencyName IN (" +
                    "SELECT DISTINCT c2.currencyName FROM CurrencyEntity c2)", 
                    CurrencyEntity.class)
                    .list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }

    public Set<CurrencyEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery("from CurrencyEntity", CurrencyEntity.class).list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
