package com.github.yuqingliu.economy.persistence.services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.AuctionEntity;
import com.github.yuqingliu.economy.persistence.entities.BidEntity;
import com.github.yuqingliu.economy.persistence.repositories.AuctionRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuctionService {
    private final AuctionRepository auctionRepository;

    public boolean startAuction(Player player, ItemStack item, double startingBid, String currencyType, Duration duration) {
        return auctionRepository.createAuction(player, item, startingBid, currencyType, Instant.now(), duration);
    }

    public boolean startAuction(Player player, ItemStack item, double startingBid, String currencyType, Instant start, Duration duration) {
        return auctionRepository.createAuction(player, item, startingBid, currencyType, start, duration);
    }

    public boolean collectAuctionItem(Player player, AuctionEntity auction) {
        return auctionRepository.collectWin(auction, player);
    }

    public boolean collectAuction(Player player, AuctionEntity auction) {
        return auctionRepository.collectAuction(auction, player);
    }

    public boolean collectBid(Player player, BidEntity bid) {
        return auctionRepository.collectLoss(bid, player);
    }

    public boolean bid(AuctionEntity auction, Player player, double bidAmount) {
        return auctionRepository.bid(auction, player, bidAmount);
    }

    public AuctionEntity getAuction(UUID auctionId) {
        return auctionRepository.getAuction(auctionId);
    }

    public List<AuctionEntity> getPlayerAuctions(Player player) {
        return auctionRepository.getPlayerAuctions(player.getUniqueId());
    }

    public List<AuctionEntity> getAuctionsWon(Player player) {
        return auctionRepository.getAuctionsWon(player.getUniqueId());
    }

    public List<AuctionEntity> getAuctionsLost(Player player) {
        return auctionRepository.getAuctionsLost(player.getUniqueId());
    }

    public List<AuctionEntity> getActiveAuctions() {
        return auctionRepository.getActiveAuctions();
    }
}
