package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PurseRepository {
    private final Hibernate hibernate;
    
    // Transactions
    public boolean save(PurseEntity purse) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            session.persist(purse);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean delete(UUID playerId) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            PurseEntity purse = session.get(PurseEntity.class, playerId);
            if (purse != null) {
                session.remove(purse);
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
    
    // Queries
    public PurseEntity get(UUID playerId) {
        try (Session session = hibernate.getSession()) {
            return session.get(PurseEntity.class, playerId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<PurseEntity> findAll() {
        try (Session session = hibernate.getSession()) {
            return Set.copyOf(session.createQuery("from PurseEntity", PurseEntity.class).list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
