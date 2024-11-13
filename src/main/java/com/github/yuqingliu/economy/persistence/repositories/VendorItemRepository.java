package com.github.yuqingliu.economy.persistence.repositories;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VendorItemRepository {
    private final SessionFactory sessionFactory;
    private final VendorItemRepository vendorItemRepository;
    
    // Transactions
    public boolean save(VendorItemEntity item) {
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

    public boolean updateVendorItem(String vendorName, String sectionName, ItemStack icon, Map<String, Double> buyPrices, Map<String, Double> sellPrices) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            String itemName = PlainTextComponentSerializer.plainText().serialize(icon.displayName());
            VendorItemEntity item = vendorItemRepository.get(new VendorItemKey(itemName, sectionName, vendorName));
            if(item == null) {
                throw new IllegalArgumentException();
            }
            item.getBuyPrices().putAll(buyPrices);
            item.getSellPrices().putAll(sellPrices);
            session.merge(item);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    public boolean delete(VendorItemKey key) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
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
            transaction.rollback();
            return false;
        }
    }
    
    // Queries
    public VendorItemEntity get(VendorItemKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(VendorItemEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }
}
