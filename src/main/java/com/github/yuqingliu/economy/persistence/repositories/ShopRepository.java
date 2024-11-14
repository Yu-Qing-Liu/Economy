package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ShopRepository {
    private final Hibernate hibernate;
    
    // Transactions
    public boolean save(ShopEntity shop) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
            transaction = session.beginTransaction();
            session.persist(shop);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    public boolean delete(String shopName) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
            transaction = session.beginTransaction();
            ShopEntity player = session.get(ShopEntity.class, shopName);
            if (player != null) {
                session.remove(player);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    // Queries
    public ShopEntity get(String shopName) {
        try (Session session = hibernate.getSession()) {
            return session.get(ShopEntity.class, shopName);
        } catch (Exception e) {
            return null;
        }
    }
}
