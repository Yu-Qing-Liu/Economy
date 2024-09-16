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
@RequiredArgsConstructor
public class PurseRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public void save(PurseEntity purse) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(purse);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(PurseEntity purse) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(purse);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PurseEntity get(UUID playerId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(PurseEntity.class, playerId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void delete(UUID playerId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            PurseEntity purse = session.get(PurseEntity.class, playerId);
            if (purse != null) {
                session.remove(purse);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
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
