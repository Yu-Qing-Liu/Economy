package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
public class AuctionEntity {
    @Id
    @GeneratedValue
    @Column(name = "auctionId", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID auctionId;

    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private UUID playerId;

    @Column(name = "highestBidder", columnDefinition = "VARCHAR(36)")
    private UUID bidderId;
    
    @Column(name = "displayName")
    private String displayName;

    @Column(name = "item", columnDefinition = "BLOB")
    private byte[] item;

    @Column(name = "start")
    private Instant start;

    @Column(name = "end")
    private Instant end;

    @Column(name = "currencyType")
    private String currencyType;

    @Column(name = "bid")
    private double bid = 0;

    @Column(name = "collected")
    private boolean collected;

    @ManyToOne
    @JoinColumn(name = "playerId", insertable = false, updatable = false)
    private PlayerEntity player;

    public ItemStack getItem() {
        return ItemStack.deserializeBytes(this.item);
    }

    public void setItem(ItemStack itemStack) {
        this.item = itemStack.serializeAsBytes();
    }
}
