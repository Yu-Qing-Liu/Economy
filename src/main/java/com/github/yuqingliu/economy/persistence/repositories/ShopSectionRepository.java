package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopSectionKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class ShopSectionRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public boolean save(ShopSectionEntity section) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(section);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ShopSectionEntity section) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(section);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ShopSectionEntity get(ShopSectionKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ShopSectionEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(ShopSectionKey key) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
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
            e.printStackTrace();
            return false;
        }
    }
}
