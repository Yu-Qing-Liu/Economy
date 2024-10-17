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
@RequiredArgsConstructor
public class ShopItemRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public boolean save(ShopItemEntity item) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(item);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ShopItemEntity item) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(item);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ShopItemEntity get(ShopItemKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ShopItemEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(ShopItemKey key) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
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
            e.printStackTrace();
            return false;
        }
    }
}
