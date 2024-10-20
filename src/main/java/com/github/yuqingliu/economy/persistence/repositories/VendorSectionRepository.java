package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.VendorEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorSectionKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class VendorSectionRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public boolean save(VendorSectionEntity section) {
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

    public boolean update(VendorSectionEntity section) {
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

    public VendorSectionEntity get(VendorSectionKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(VendorSectionEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(VendorSectionKey key) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            VendorSectionEntity section = session.get(VendorSectionEntity.class, key);
            VendorEntity vendor = session.get(VendorEntity.class, key.getVendorName());
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
