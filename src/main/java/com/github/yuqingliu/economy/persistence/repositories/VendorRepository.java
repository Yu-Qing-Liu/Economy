package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.VendorEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VendorRepository {
    private final SessionFactory sessionFactory;
    
    // Transactions
    public boolean save(VendorEntity vendor) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(vendor);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean delete(String vendorName) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            VendorEntity player = session.get(VendorEntity.class, vendorName);
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
    public VendorEntity get(String vendorName) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(VendorEntity.class, vendorName);
        } catch (Exception e) {
            return null;
        }
    }
}
