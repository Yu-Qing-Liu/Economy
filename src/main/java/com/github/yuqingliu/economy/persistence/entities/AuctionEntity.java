package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
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

    @Column(name = "displayName")
    private String displayName;

    @Column(name = "item", columnDefinition = "BLOB")
    private byte[] item;

    @Column(name = "start")
    private Instant start;

    @Column(name = "end")
    private Instant end;

    @Column(name = "highestBid")
    private double highestBid;

    @Column(name = "bidderId")
    private UUID bidderId;

    @Column(name = "currencyType")
    private String currencyType;

    @Column(name = "collected")
    private boolean collected = false;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("amount DESC")
    private Set<BidEntity> bids = new LinkedHashSet<>();

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
