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
    public boolean createAuction(Player player, ItemStack item, double startingBid, String currencyType, Instant start,
            Duration duration) {
        Transaction transaction = null;
        Session session = hibernate.getSession();
        try {
            transaction = session.beginTransaction();
            Query<CurrencyEntity> query = session.createQuery(
                    "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                    CurrencyEntity.class);
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if (purseCurrency.getAmount() < startingBid) {
                logger.sendPlayerErrorMessage(player, "You do not have enough currency to start this auction");
                throw new RuntimeException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - startingBid);
            session.merge(purseCurrency);
            AuctionEntity auctionEntity = new AuctionEntity();
            Instant end = start.plus(duration);
            auctionEntity.setPlayerId(player.getUniqueId());
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
            ItemStack temp = auction.getItem().clone();
            boolean refund = false;
            if (!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if (!auction.getPlayerId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "This auction does not belong to you.");
                throw new RuntimeException();
            }
            if (auction.isRefunded()) {
                logger.sendPlayerErrorMessage(player, "You have already collected this auction.");
                throw new RuntimeException();
            }
            if (auction.getBidderId() == null) {
                refund = true;
            }
            Query<CurrencyEntity> query = session.createQuery(
                    "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                    CurrencyEntity.class);
            query.setParameter("purseId", auction.getPlayerId());
            query.setParameter("currencyName", auction.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + auction.getHighestBid());
            session.merge(purseCurrency);
            auction.setRefunded(true);
            session.merge(auction);
            if (canRemoveAuction(auction) || refund) {
                session.remove(auction);
            }
            if (refund) {
                inventoryManager.addItemToPlayer(player, temp, 1);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
            if (!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if (!auction.getBidderId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "You did not win this auction.");
                throw new RuntimeException();
            }
            if (auction.isCollected()) {
                logger.sendPlayerErrorMessage(player, "You already collected this auction");
                throw new RuntimeException();
            }
            ItemStack item = auction.getItem().clone();
            auction.setCollected(true);
            session.merge(auction);
            Query<BidEntity> query = session.createQuery(
                    "FROM BidEntity WHERE playerId = :playerId AND auctionId = :auctionId",
                    BidEntity.class);
            query.setParameter("playerId", auction.getBidderId());
            query.setParameter("auctionId", auction.getAuctionId());
            BidEntity bid = query.uniqueResult();
            if (bid != null) {
                Query<CurrencyEntity> purseQuery = session.createQuery(
                        "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                        CurrencyEntity.class);
                purseQuery.setParameter("purseId", auction.getBidderId());
                purseQuery.setParameter("currencyName", auction.getCurrencyType());
                CurrencyEntity purseCurrency = purseQuery.uniqueResult();
                purseCurrency.setAmount(purseCurrency.getAmount() + bid.getAmount());
                session.merge(purseCurrency);
                auction.getBids().remove(bid);
                session.merge(auction);
            }
            if (canRemoveAuction(auction)) {
                session.remove(auction);
            }
            inventoryManager.addItemToPlayer(player, item, 1);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
            if (!auction.getEnd().isBefore(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has not ended yet.");
                throw new RuntimeException();
            }
            if (auction.getBidderId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "Cannot collect bid on an auction that you won.");
                throw new RuntimeException();
            }
            if (!bid.getPlayerId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "You did not bid on this auction.");
                throw new RuntimeException();
            }
            Query<CurrencyEntity> query = session.createQuery(
                    "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                    CurrencyEntity.class);
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", auction.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            purseCurrency.setAmount(purseCurrency.getAmount() + bid.getAmount());
            session.merge(purseCurrency);
            auction.getBids().remove(bid);
            session.merge(auction);
            if (canRemoveAuction(auction)) {
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
            if (!auction.getEnd().isAfter(Instant.now())) {
                logger.sendPlayerErrorMessage(player, "This auction has ended already.");
                throw new RuntimeException();
            }
            if (auction.getPlayerId().equals(player.getUniqueId())) {
                logger.sendPlayerErrorMessage(player, "Cannot bid on your own auctions");
                throw new RuntimeException();
            }
            Query<CurrencyEntity> query = session.createQuery(
                    "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName",
                    CurrencyEntity.class);
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", auction.getCurrencyType());
            CurrencyEntity purseCurrency = query.uniqueResult();
            if (bidAmount <= auction.getHighestBid() || purseCurrency.getAmount() < bidAmount) {
                logger.sendPlayerErrorMessage(player, "Not enough currency to bid higher.");
                throw new RuntimeException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - bidAmount);
            session.merge(purseCurrency);
            Query<BidEntity> getPreviousBid = session.createQuery(
                    "FROM BidEntity WHERE playerId = :playerId AND auctionId = :auctionId",
                    BidEntity.class);
            getPreviousBid.setParameter("playerId", auction.getBidderId());
            getPreviousBid.setParameter("auctionId", auction.getAuctionId());
            BidEntity previousBid = getPreviousBid.uniqueResult();
            if (previousBid != null) {
                previousBid.setAmount(previousBid.getAmount() + auction.getHighestBid());
                session.merge(previousBid);
            } else if (auction.getBidderId() != null) {
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
            logger.sendPlayerNotificationMessage(player,
                    String.format("You bid %.2f %s", bidAmount, auction.getCurrencyType()));
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    // Queries
    public AuctionEntity getAuction(UUID auctionId) {
        try (Session session = hibernate.getSession()) {
            String hql = "FROM AuctionEntity WHERE auctionId = :auctionId";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            query.setParameter("auctionId", auctionId);
            return query.uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<AuctionEntity> getPlayerAuctions(UUID playerId) {
        try (Session session = hibernate.getSession()) {
            String hql = "FROM AuctionEntity WHERE playerId = :playerId";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            query.setParameter("playerId", playerId);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<AuctionEntity> getAuctionsWon(UUID playerId) {
        try (Session session = hibernate.getSession()) {
            String hql = "FROM AuctionEntity WHERE bidderId = :bidderId AND highestBid > 0";
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
            String hql = "FROM AuctionEntity";
            Query<AuctionEntity> query = session.createQuery(hql, AuctionEntity.class);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public BidEntity getPlayerBid(AuctionEntity auction, Player player) {
        try (Session session = hibernate.getSession()) {
            Query<BidEntity> query = session.createQuery(
                    "FROM BidEntity WHERE playerId = :playerId AND auctionId = :auctionId",
                    BidEntity.class);
            query.setParameter("playerId", player.getUniqueId());
            query.setParameter("auctionId", auction.getAuctionId());
            return query.uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    // Helpers
    private boolean canRemoveAuction(AuctionEntity auction) {
        if (auction.getBids() != null && !auction.getBids().isEmpty()) {
            return false;
        }
        if (!auction.isCollected() || !auction.isRefunded()) {
            return false;
        }
        return true;
    }
}
