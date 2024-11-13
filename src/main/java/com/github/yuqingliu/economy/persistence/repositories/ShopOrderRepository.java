package com.github.yuqingliu.economy.persistence.repositories;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
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
    private final InventoryManager inventoryManager;
    private final Logger logger;
    
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

    public boolean createBuyOrder(Player player, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        UUID playerId = player.getUniqueId();
        double cost = unitPrice * quantity;
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", playerId);
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(purseCurrency.getAmount() < cost) {
                logger.sendPlayerErrorMessage(player, "Not enough currency.");
                throw new IllegalArgumentException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - cost);
            session.merge(purseCurrency);
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

    public boolean createSellOrder(Player player, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        Transaction transaction = null;
        UUID playerId = player.getUniqueId();
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            if(!inventoryManager.removeItemFromPlayer(player, item.getIcon().clone(), quantity)) {
                logger.sendPlayerErrorMessage(player, "Not enough items to be sold.");
                throw new RuntimeException();
            }
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

    public boolean cancelBuyOrder(ShopOrderEntity order, Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            UUID playerId = order.getPlayerId();
            order.setQuantity(order.getQuantity() - order.getFilledQuantity());
            int amount = order.getFilledQuantity();
            double refund = order.getQuantity() * order.getUnitPrice();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", playerId);
            query.setParameter("currencyName", order.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + refund);
            session.merge(purseCurrency);
            inventoryManager.addItemToPlayer(player, order.getShopItem().getIcon().clone(), amount);
            session.remove(order);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean cancelSellOrder(ShopOrderEntity order, Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            UUID playerId = order.getPlayerId();
            order.setQuantity(order.getQuantity() - order.getFilledQuantity());
            int amount = order.getQuantity();
            double profit = order.getFilledQuantity() * order.getUnitPrice();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", playerId);
            query.setParameter("currencyName", order.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + profit);
            session.merge(purseCurrency);
            inventoryManager.addItemToPlayer(player, order.getShopItem().getIcon().clone(), amount);
            session.remove(order);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean claimBuyOrder(ShopOrderEntity order, Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            int amount = order.getFilledQuantity();
            order.setQuantity(order.getQuantity() - order.getFilledQuantity());
            order.setFilledQuantity(0);
            if(order.getQuantity() > 0) {
                session.merge(order);
            } else {
                session.remove(order);
            }
            inventoryManager.addItemToPlayer(player, order.getShopItem().getIcon().clone(), amount);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean claimSellOrder(ShopOrderEntity order, Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            double profit = order.getFilledQuantity();
            order.setQuantity(order.getQuantity() - order.getFilledQuantity());
            order.setFilledQuantity(0);
            if(order.getQuantity() > 0) {
                session.merge(order);
            } else {
                session.remove(order);
            }
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", order.getPlayerId());
            query.setParameter("currencyName", order.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + profit);
            session.merge(purseCurrency);
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
