package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PurseRepository {
    private final SessionFactory sessionFactory;
    
    // Transactions
    public boolean save(PurseEntity purse) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(purse);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean delete(UUID playerId) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
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
        }
    }
    
    // Queries
    public PurseEntity get(UUID playerId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(PurseEntity.class, playerId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<PurseEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery("from PurseEntity", PurseEntity.class).list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
