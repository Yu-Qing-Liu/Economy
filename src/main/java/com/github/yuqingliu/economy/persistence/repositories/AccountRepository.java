package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class AccountRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public void save(AccountEntity account) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(account);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(AccountEntity account) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(account);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AccountEntity get(UUID accountId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(AccountEntity.class, accountId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void delete(UUID accountId) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            AccountEntity account = session.get(AccountEntity.class, accountId);
            if (account != null) {
                session.remove(account);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<AccountEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery("from AccountEntity", AccountEntity.class).list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
