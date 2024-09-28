package com.github.yuqingliu.economy.persistence.repositories;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
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
            if (currency != null) {
                session.remove(currency);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
