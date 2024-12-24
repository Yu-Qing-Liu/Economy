package com.github.yuqingliu.economy.persistence.repositories;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.AuctionEntity;
import com.github.yuqingliu.economy.persistence.entities.BidEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuctionRepository {
    private final Hibernate hibernate;
    private final InventoryManager inventoryManager;
    private final Logger logger;

    // Transactions 
    public boolean createAuction(Player player, ItemStack item, double startingBid, String currencyType, Instant start, Duration duration) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            AuctionEntity auctionEntity = new AuctionEntity();
            Instant end = start.plus(duration);
            auctionEntity.setStart(start);
            auctionEntity.setEnd(end);
            auctionEntity.setHighestBid(startingBid);
            auctionEntity.setItem(item);
            auctionEntity.setDisplayName(PlainTextComponentSerializer.plainText().serialize(item.displayName()));
            auctionEntity.setCurrencyType(currencyType);
            session.persist(auctionEntity);
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
            if(!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if(!auction.getPlayerId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "This auction does not belong to you.");
                throw new RuntimeException();
            }
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                CurrencyEntity.class
            );
            query.setParameter("purseId", auction.getPlayerId());
            query.setParameter("currencyName", auction.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + auction.getHighestBid());
            session.merge(purseCurrency);
            auction.setHighestBid(0);
            session.merge(auction);
            if(canRemoveAuction(auction)) {
                session.remove(auction);
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

    public boolean collectWin(AuctionEntity auction, Player player) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            if(!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if(!auction.getBidderId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "You did not win this auction.");
                throw new RuntimeException();
            }
            ItemStack item = auction.getItem().clone();
            auction.setCollected(true);
            session.merge(auction);
            inventoryManager.addItemToPlayer(player, item, 1);
            Query<BidEntity> query = session.createQuery(
                "FROM BidEntity WHERE playerId = :playerId AND auctionId = :auctionId",
                BidEntity.class
            );
            query.setParameter("playerId", auction.getBidderId());
            query.setParameter("auctionId", auction.getAuctionId());
            BidEntity bid = query.uniqueResult();
            if(bid != null) {
                session.remove(bid);
            }
            if(canRemoveAuction(auction)) {
                session.remove(auction);
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

    public boolean collectLoss(BidEntity bid, Player player) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            AuctionEntity auction = bid.getAuction();
            if(!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if(auction.getBidderId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "Cannot collect bid on an auction that you won.");
                throw new RuntimeException();
            }
            if(!bid.getPlayerId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "You did not bid on this auction.");
                throw new RuntimeException();
            }
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", auction.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + bid.getAmount());
            session.merge(purseCurrency);
            session.remove(bid);
            if(canRemoveAuction(auction)) {
                session.remove(auction);
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

    public boolean bid(AuctionEntity auction, Player player, double bidAmount) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            if(!auction.getEnd().isAfter(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has ended already.");
                throw new RuntimeException();
            }
            if(!auction.getPlayerId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "Cannot bid on your own auctions");
                throw new RuntimeException();
            }
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", auction.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(bidAmount <= auction.getHighestBid() || purseCurrency.getAmount() < bidAmount) {
                logger.sendPlayerErrorMessage(player, "Not enough currency to bid higher.");
                throw new RuntimeException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - bidAmount);
            session.merge(purseCurrency);
            Query<BidEntity> getPreviousBid = session.createQuery(
                "FROM BidEntity WHERE playerId = :playerId AND auctionId = :auctionId",
                BidEntity.class
            );
            getPreviousBid.setParameter("playerId", auction.getBidderId());
            getPreviousBid.setParameter("auctionId", auction.getAuctionId());
            BidEntity previousBid = getPreviousBid.uniqueResult();
            if(previousBid != null) {
                previousBid.setAmount(auction.getHighestBid());
                session.merge(previousBid);
            } else {
                previousBid = new BidEntity();
                previousBid.setAmount(auction.getHighestBid());
                previousBid.setPlayerId(auction.getBidderId());
                previousBid.setAuctionId(auction.getAuctionId());
                session.persist(previousBid);
            }
            auction.setHighestBid(bidAmount);
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
            String hql = "FROM AuctionEntity WHERE bidderId = :bidderId AND end > CURRENT_TIMESTAMP AND highestBid > 0";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            query.setParameter("bidderId", playerId);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<AuctionEntity> getAuctionsLost(UUID playerId) {
        try (Session session = hibernate.getSession()) {
            String hql = "FROM AuctionEntity a JOIN BidEntity b ON b.auctionId = a.auctionId WHERE b.playerId = :playerId AND b.playerId != a.bidderId";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<AuctionEntity> getActiveAuctions() {
        try (Session session = hibernate.getSession()) {
            String hql = "FROM AuctionEntity WHERE end < CURRENT_TIMESTAMP";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    // Helpers
    private boolean canRemoveAuction(AuctionEntity auction) {
        if(auction.getBids() != null && !auction.getBids().isEmpty()) {
            return false;
        }
        if(!auction.isCollected()) {
            return false;
        }
        if(!(auction.getHighestBid() > 0)) {
            return false;
        }
        return true;
    }
}
