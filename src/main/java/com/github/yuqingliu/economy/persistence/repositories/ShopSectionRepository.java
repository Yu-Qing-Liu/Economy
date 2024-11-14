package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopSectionKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ShopSectionRepository {
    private final Hibernate hibernate;
    
    // Transactions
    public boolean save(ShopSectionEntity section) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            session.persist(section);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }
    
    public boolean delete(ShopSectionKey key) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            ShopSectionEntity section = session.get(ShopSectionEntity.class, key);
            ShopEntity vendor = session.get(ShopEntity.class, key.getShopName());
            if (section != null) {
                vendor.getSections().remove(section);
                session.remove(section);
                session.persist(vendor);
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
    public ShopSectionEntity get(ShopSectionKey key) {
        try (Session session = hibernate.getSession()) {
            return session.get(ShopSectionEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    
}
