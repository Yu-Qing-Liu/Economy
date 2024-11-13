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
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VendorSectionRepository {
    private final SessionFactory sessionFactory;

    public boolean save(VendorSectionEntity section) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(section);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean delete(VendorSectionKey key) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
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
            transaction.rollback();
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
}
