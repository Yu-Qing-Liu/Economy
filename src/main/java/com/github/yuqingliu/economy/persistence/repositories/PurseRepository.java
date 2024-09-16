package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class PurseRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public void update(PurseEntity purse) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(purse);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PurseEntity get(UUID uuid) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(PurseEntity.class, uuid);
        } catch (Exception e) {
            return null;
        }
    }
}
