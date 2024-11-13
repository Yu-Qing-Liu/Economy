package com.github.yuqingliu.economy.persistence.repositories;

import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity.OrderType;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopOrderKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ShopOrderRepository {
    private final SessionFactory sessionFactory;
    private final CurrencyRepository currencyRepository;
    
    // Transactions
    public boolean save(ShopOrderEntity order) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(order);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    public boolean delete(ShopOrderKey key) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            ShopOrderEntity order = session.get(ShopOrderEntity.class, key);
            ShopItemEntity item = session.get(ShopItemEntity.class, new ShopItemKey(key.getItemName(), key.getSectionName(), key.getShopName()));
            if (order != null) {
                item.getOrders().remove(order);
                session.remove(order);
                session.persist(item);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean createBuyOrder(UUID playerId, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        double cost = unitPrice * quantity;
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            if(!currencyRepository.withdrawPlayerPurse(playerId, currencyType, cost)) {
                throw new IllegalArgumentException();
            }
            ShopOrderEntity order = new ShopOrderEntity();
            order.setType(OrderType.BUY);
            order.setPlayerId(playerId);
            order.setItemName(item.getItemName());
            order.setSectionName(item.getSectionName());
            order.setShopName(item.getShopName());
            order.setQuantity(quantity);
            order.setUnitPrice(unitPrice);
            order.setCurrencyType(currencyType);
            order.setShopItem(item);
            session.persist(order);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean createSellOrder(UUID playerId, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            ShopOrderEntity order = new ShopOrderEntity();
            order.setType(OrderType.SELL);
            order.setPlayerId(playerId);
            order.setItemName(item.getItemName());
            order.setSectionName(item.getSectionName());
            order.setShopName(item.getShopName());
            order.setQuantity(quantity);
            order.setUnitPrice(unitPrice);
            order.setCurrencyType(currencyType);
            order.setShopItem(item);
            session.persist(order);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    // Queries
    public ShopOrderEntity get(ShopOrderKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ShopOrderEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public List<ShopOrderEntity> getBuyOrdersByPlayer(UUID playerId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM ShopOrderEntity WHERE playerId = :playerId AND type = :orderType";
            Query<ShopOrderEntity> query = session.createQuery(hql, ShopOrderEntity.class);
            query.setParameter("playerId", playerId);
            query.setParameter("orderType", OrderType.BUY);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<ShopOrderEntity> getSellOrdersByPlayer(UUID playerId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM ShopOrderEntity WHERE playerId = :playerId AND type = :orderType";
            Query<ShopOrderEntity> query = session.createQuery(hql, ShopOrderEntity.class);
            query.setParameter("playerId", playerId);
            query.setParameter("orderType", OrderType.SELL);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }
}
