package com.github.yuqingliu.economy.persistence.repositories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.AuctionEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuctionRepository {
    private final Hibernate hibernate;
    private final InventoryManager inventoryManager;
    private final Logger logger;

    // Transactions 
    public boolean collectPay(AuctionEntity auction, Player player) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            UUID playerId = auction.getPlayerId();
            String currencyType = auction.getCurrencyType();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                CurrencyEntity.class
            );
            query.setParameter("purseId", playerId);
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if(auction.isCollected()) {
                session.remove(auction);
            } else {
                auction.setBid(0);
                session.merge(auction);
            }
            purseCurrency.setAmount(purseCurrency.getAmount() + auction.getBid());
            session.merge(purseCurrency);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean collectAuction(AuctionEntity auction, Player player) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            ItemStack item = auction.getItem().clone();
            if(!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if(auction.getBid() == 0) {
                session.remove(auction);
            } else {
                auction.setCollected(true);
                session.merge(auction);
            }
            inventoryManager.addItemToPlayer(player, item, 1);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    public boolean collectBid(PlayerEntity player) {
        return false;
    }

    public boolean bid(AuctionEntity auction, Player player, double bidAmount) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", auction.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(bidAmount <= auction.getBid() || purseCurrency.getAmount() < bidAmount) {
                logger.sendPlayerErrorMessage(player, "Not enough currency to bid higher.");
                throw new RuntimeException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - bidAmount);
            session.merge(purseCurrency);
            UUID previousWinner = auction.getBidderId();
            Query<PlayerEntity> playerQuery = session.createQuery(
                "From PlayerEntity WHERE playerId = :playerId",
                PlayerEntity.class
            );
            playerQuery.setParameter("playerId", previousWinner);
            PlayerEntity prevPlayer = playerQuery.uniqueResult();
            prevPlayer.getBids().put(auction.getAuctionId(), auction.getBid());
            session.merge(prevPlayer);
            auction.setBid(bidAmount);
            auction.setBidderId(player.getUniqueId());
            session.merge(auction);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }
    
    // Queries
    public List<AuctionEntity> getAuctionsWon(UUID playerId) {
        try (Session session = hibernate.getSession()) {
            String hql = "FROM AuctionEntity WHERE bidderId = :bidderId AND end < CURRENT_TIMESTAMP";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            query.setParameter("bidderId", playerId);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<AuctionEntity> getPlayerAuctions(UUID playerId) {
        try (Session session = hibernate.getSession()) {
            List<AuctionEntity> auctions = new ArrayList<>();
            Query<PlayerEntity> query = session.createQuery(
                "From PlayerEntity WHERE playerId = :playerId",
                PlayerEntity.class
            );
            query.setParameter("playerId", playerId);
            PlayerEntity player = query.uniqueResult();
            Query<AuctionEntity> auctionQuery = session.createQuery(
                "From AuctionEntity WHERE auctionId = :auctionId",
                AuctionEntity.class
            );
            for (UUID auctionId : player.getBids().keySet()) {
                auctionQuery.setParameter("auctionId", auctionId);
                auctions.add(auctionQuery.uniqueResult());
            }
            return auctions;
        } catch (Exception e) {
            return null;
        }
    }

    public List<AuctionEntity> getActiveAuctions() {
        try (Session session = hibernate.getSession()) {
            String hql = "FROM AuctionEntity WHERE end > CURRENT_TIMESTAMP";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }
}
