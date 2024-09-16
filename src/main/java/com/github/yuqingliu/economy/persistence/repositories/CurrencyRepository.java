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

    public void save(CurrencyEntity currency) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(currency);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(CurrencyEntity currency) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(currency);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CurrencyEntity get(CurrencyKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(CurrencyEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public void delete(CurrencyKey key) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            CurrencyEntity currency = session.find(CurrencyEntity.class, key);
            if (currency != null) {
                session.remove(currency);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
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
