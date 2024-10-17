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
@RequiredArgsConstructor
public class ShopOrderRepository {
    @Inject
    private final SessionFactory sessionFactory;

    public boolean save(ShopOrderEntity order) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(order);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ShopOrderEntity order) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(order);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ShopOrderEntity get(ShopOrderKey key) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ShopOrderEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(ShopOrderKey key) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
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
            e.printStackTrace();
            return false;
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
            e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }
}
