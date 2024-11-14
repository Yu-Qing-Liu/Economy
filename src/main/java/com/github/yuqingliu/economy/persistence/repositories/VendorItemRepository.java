package com.github.yuqingliu.economy.persistence.repositories;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
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
    private final Hibernate hibernate;
    private final Logger logger;
    private final InventoryManager inventoryManager;
    private final SoundManager soundManager;
    
    // Transactions
    public boolean save(VendorItemEntity item) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            session.persist(item);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean updateVendorItem(String vendorName, String sectionName, ItemStack icon, Map<String, Double> buyPrices, Map<String, Double> sellPrices) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            String itemName = PlainTextComponentSerializer.plainText().serialize(icon.displayName());
            VendorItemEntity item = this.get(new VendorItemKey(itemName, sectionName, vendorName));
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
        } finally {
            session.close();
        }
    }
    
    public boolean delete(VendorItemKey key) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
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
        } finally {
            session.close();
        }
    }

    public boolean buy(VendorItemEntity item, int amount, String currencyType, Player player) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            double cost = amount * item.getBuyPrices().get(currencyType);
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(purseCurrency.getAmount() < cost) {
                logger.sendPlayerErrorMessage(player, "Not enough currency.");
                throw new RuntimeException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - cost);
            session.merge(purseCurrency);
            inventoryManager.addItemToPlayer(player, item.getIcon().clone(), amount);
            transaction.commit();
            logger.sendPlayerNotificationMessage(player, String.format("Bought %d item(s) for %.2f %s", amount, cost, currencyType));
            soundManager.playTransactionSound(player);
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean sell(VendorItemEntity item, int amount, String currencyType, Player player) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            double profit = amount * item.getBuyPrices().get(currencyType);
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(!inventoryManager.removeItemFromPlayer(player, item.getIcon().clone(), amount)) {
                logger.sendPlayerErrorMessage(player, "Not enough items to be sold.");
                throw new RuntimeException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() + profit);
            session.merge(purseCurrency);
            transaction.commit();
            logger.sendPlayerNotificationMessage(player, String.format("Sold %d item(s) for %.2f %s", amount, profit, currencyType));
            soundManager.playTransactionSound(player);
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }
    
    // Queries
    public VendorItemEntity get(VendorItemKey key) {
        try (Session session = hibernate.getSession()) {
            return session.get(VendorItemEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }
}
