package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorSectionKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class VendorItemRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public boolean save(VendorItemEntity item) {
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

    public boolean update(VendorItemEntity item) {
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

    public VendorItemEntity get(VendorItemKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(VendorItemEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(VendorItemKey key) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            VendorItemEntity item = session.get(VendorItemEntity.class, key);
            VendorSectionEntity section = session.get(VendorSectionEntity.class, new VendorSectionKey(key.getSectionName(), key.getVendorName()));
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
