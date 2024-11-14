package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.VendorEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VendorRepository {
    private final Hibernate hibernate;
    
    // Transactions
    public boolean save(VendorEntity vendor) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
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
        try (Session session = hibernate.getSession()) {
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
        try (Session session = hibernate.getSession()) {
            return session.get(VendorEntity.class, vendorName);
        } catch (Exception e) {
            return null;
        }
    }
}
