package com.github.yuqingliu.economy.persistence.repositories;

import java.util.Set;

import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopSectionKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ShopItemRepository {
    private final Hibernate hibernate;
    private final InventoryManager inventoryManager;
    private final Logger logger;
    private final SoundManager soundManager;

    // Transactions
    public boolean save(ShopItemEntity item) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
            transaction = session.beginTransaction();
            session.persist(item);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }
    
    public boolean delete(ShopItemKey key) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
            transaction = session.beginTransaction();
            ShopItemEntity item = session.get(ShopItemEntity.class, key);
            ShopSectionEntity section = session.get(ShopSectionEntity.class, new ShopSectionKey(key.getSectionName(), key.getShopName()));
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

    public boolean quickBuy(ShopItemEntity item, int amount, String currencyType, Player player) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
            transaction = session.beginTransaction();
            Set<ShopOrderEntity> sellOffers = item.getSellOrders().get(currencyType);
            int required = amount;
            double cost = 0;
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            for(ShopOrderEntity order : sellOffers) {
                if(order.getQuantity() == order.getFilledQuantity()) {
                    continue;
                }
                int qty = order.getQuantity() - order.getFilledQuantity();
                if(qty > required) {
                    cost += required * order.getUnitPrice();
                    order.setFilledQuantity(order.getFilledQuantity() + required);
                    if(purseCurrency.getAmount() < required * order.getUnitPrice()) {
                        logger.sendPlayerErrorMessage(player, "Not enough currency.");
                        throw new IllegalArgumentException();
                    }
                    purseCurrency.setAmount(purseCurrency.getAmount() - required * order.getUnitPrice());
                    session.merge(purseCurrency);
                    session.merge(order);
                    inventoryManager.addItemToPlayer(player, item.getIcon().clone(), required);
                    required = 0;
                    break;
                } else if(qty == required) {
                    cost += required * order.getUnitPrice();
                    order.setFilledQuantity(order.getFilledQuantity() + required);
                    if(purseCurrency.getAmount() < required * order.getUnitPrice()) {
                        logger.sendPlayerErrorMessage(player, "Not enough currency.");
                        throw new IllegalArgumentException();
                    }
                    purseCurrency.setAmount(purseCurrency.getAmount() - required * order.getUnitPrice());
                    session.merge(purseCurrency);
                    session.merge(order);
                    inventoryManager.addItemToPlayer(player, item.getIcon().clone(), required);
                    required = 0;
                    break;
                } else {
                    cost += qty * order.getUnitPrice();
                    order.setFilledQuantity(order.getFilledQuantity() + qty);
                    if(purseCurrency.getAmount() < qty * order.getUnitPrice()) {
                        logger.sendPlayerErrorMessage(player, "Not enough currency.");
                        throw new IllegalArgumentException();
                    }
                    purseCurrency.setAmount(purseCurrency.getAmount() - qty * order.getUnitPrice());
                    session.merge(purseCurrency);
                    session.merge(order);
                    inventoryManager.addItemToPlayer(player, item.getIcon().clone(), qty);
                    required -= qty;
                }
            };
            if(required != amount) {
                logger.sendPlayerNotificationMessage(player, String.format("Bought %d item(s) for %.2f %s", amount - required, cost, currencyType));
                soundManager.playTransactionSound(player);
            } else {
                logger.sendPlayerErrorMessage(player, "No more offers.");
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean quickSell(ShopItemEntity item, int amount, String currencyType, Player player) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
            transaction = session.beginTransaction();
            Set<ShopOrderEntity> buyOrders = item.getBuyOrders().get(currencyType);
            int required = amount;
            double profit = 0;
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            for(ShopOrderEntity order : buyOrders) {
                if(order.getQuantity() == order.getFilledQuantity()) {
                    continue;
                }
                int qty = order.getQuantity() - order.getFilledQuantity();
                if(qty > required) {
                    order.setFilledQuantity(order.getFilledQuantity() + required);
                    purseCurrency.setAmount(purseCurrency.getAmount() + required * order.getUnitPrice());
                    session.merge(purseCurrency);
                    session.merge(order);
                    if(!inventoryManager.removeItemFromPlayer(player, item.getIcon().clone(), required)) {
                        logger.sendPlayerErrorMessage(player, "Not enough items to be sold.");
                        throw new RuntimeException();
                    }
                    profit += required * order.getUnitPrice();
                    required = 0;
                    break;
                } else if(qty == required) {
                    order.setFilledQuantity(order.getFilledQuantity() + required);
                    purseCurrency.setAmount(purseCurrency.getAmount() + required * order.getUnitPrice());
                    session.merge(purseCurrency);
                    session.merge(order);
                    if(!inventoryManager.removeItemFromPlayer(player, item.getIcon().clone(), required)) {
                        logger.sendPlayerErrorMessage(player, "Not enough items to be sold.");
                        throw new RuntimeException();
                    }
                    profit += required * order.getUnitPrice();
                    required = 0;
                    break;
                } else {
                    order.setFilledQuantity(order.getFilledQuantity() + qty);
                    purseCurrency.setAmount(purseCurrency.getAmount() + qty * order.getUnitPrice());
                    session.merge(purseCurrency);
                    session.merge(order);
                    if(!inventoryManager.removeItemFromPlayer(player, item.getIcon().clone(), qty)) {
                        logger.sendPlayerErrorMessage(player, "Not enough items to be sold.");
                        throw new RuntimeException();
                    }
                    profit += qty * order.getUnitPrice();
                    required -= qty;
                }
            };
            if(required != amount) {
                logger.sendPlayerNotificationMessage(player, String.format("Sold %d item(s) for %.2f %s", amount - required, profit, currencyType));
                soundManager.playTransactionSound(player);
            } else {
                logger.sendPlayerErrorMessage(player, "No more offers.");
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } 
    }

    
    // Queries
    public ShopItemEntity get(ShopItemKey key) {
        try (Session session = hibernate.getSession()) {
            return session.get(ShopItemEntity.class, key);
        } catch (Exception e) {
            return null;
        }
    }
}
