package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class PlayerRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public void save(PlayerEntity player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(PlayerEntity player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(player);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlayerEntity get(UUID uuid) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(PlayerEntity.class, uuid);
        } catch (Exception e) {
            return null;
        }
    }

    public void delete(UUID uuid) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            PlayerEntity player = session.get(PlayerEntity.class, uuid);
            if (player != null) {
                session.remove(player);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
