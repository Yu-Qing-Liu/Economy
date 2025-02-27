package com.github.yuqingliu.economy.persistence.entities;

import java.util.Set;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

import com.github.yuqingliu.economy.persistence.entities.keys.VendorItemKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyClass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class PlayerEntity {
    @Id
    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private UUID playerId;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private PurseEntity purse;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("accountName ASC")
    private Set<AccountEntity> accounts = new LinkedHashSet<>();

    @Column(name = "lastVendorBuyLimitRefill", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant lastVendorBuyLimitRefill;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "vendor_items_buy_limit",
        joinColumns = {
            @JoinColumn(name = "playerId", referencedColumnName = "playerId"),
        }
    )
    @MapKeyClass(VendorItemKey.class)
    @Column(name = "quantity_bought")
    private Map<VendorItemKey, Integer> vendorItemsBuyLimit = new HashMap<>();

    public void setPurse(PurseEntity purse) {
        if (purse != null) {
            purse.setPlayer(this);
        }
        this.purse = purse;
    }

    public void refillVendorBuyLimit(int amount) {
        for (VendorItemKey key : vendorItemsBuyLimit.keySet()) {
            vendorItemsBuyLimit.put(key, amount);
        }
    }
}
