package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import com.github.yuqingliu.economy.persistence.entities.keys.BidKey;

@Entity
@Table(name = "bids")
@IdClass(BidKey.class)
@Getter
@Setter
@NoArgsConstructor
public class BidEntity {
    @Id
    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private UUID playerId;

    @Id
    @Column(name = "auctionId", columnDefinition = "VARCHAR(36)")
    private UUID auctionId;

    @Column(name = "amount")
    private double amount;

    @ManyToOne
    @JoinColumn(name = "auctionId", insertable = false, updatable = false)
    private AuctionEntity auction;

    @ManyToOne
    @JoinColumn(name = "playerId", insertable = false, updatable = false)
    private PlayerEntity player;
}
