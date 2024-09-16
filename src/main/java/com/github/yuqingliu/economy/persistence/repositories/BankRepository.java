package com.github.yuqingliu.economy.persistence.repositories;

import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.Set;

@Singleton
@RequiredArgsConstructor
public class BankRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public void save(BankEntity bank) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(bank);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(BankEntity bank) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(bank);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BankEntity get(String bankName) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(BankEntity.class, bankName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void delete(String bankName) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            BankEntity bank = session.get(BankEntity.class, bankName);
            if (bank != null) {
                session.remove(bank);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<BankEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery("from BankEntity", BankEntity.class).list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
