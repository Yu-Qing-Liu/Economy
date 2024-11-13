package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopSectionKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ShopItemRepository {
    private final SessionFactory sessionFactory;

    // Transactions
    public boolean save(ShopItemEntity item) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(item);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    public boolean delete(ShopItemKey key) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            ShopItemEntity item = session.get(ShopItemEntity.class, key);
            ShopSectionEntity section = session.get(ShopSectionEntity.class, new ShopSectionKey(key.getSectionName(), key.getShopName()));
            if (item != null) {
                section.getItems().remove(item);
                session.remove(item);
                session.persist(section);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    // Queries
    public ShopItemEntity get(ShopItemKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ShopItemEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }
}
