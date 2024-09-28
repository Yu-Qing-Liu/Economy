package com.github.yuqingliu.economy.persistence.entities;

import java.util.UUID;

import com.github.yuqingliu.economy.persistence.entities.keys.ShopOrderKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shoporders")
@IdClass(ShopOrderKey.class)
@Getter
@Setter
@NoArgsConstructor
public class ShopOrderEntity {
    public enum OrderType {
        BUY, SELL;
    }

    @Id
    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private UUID playerId;
    
    @Id
    @Column(name = "itemName", columnDefinition = "VARCHAR(16)")
    private String itemName;

    @Id
    @Column(name = "sectionName", columnDefinition = "VARCHAR(16)")
    private String sectionName;

    @Id
    @Column(name = "shopName", columnDefinition = "VARCHAR(16)")
    private String shopName;

    @ManyToOne
    @JoinColumn(name = "playerId", insertable = false, updatable = false)
    private PlayerEntity player;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "itemName", referencedColumnName = "itemName", insertable = false, updatable = false),
        @JoinColumn(name = "sectionName", referencedColumnName = "sectionName", insertable = false, updatable = false),
        @JoinColumn(name = "shopName", referencedColumnName = "shopName", insertable = false, updatable = false)
    })
    private ShopItemEntity shopItem;

    @Column(name = "quantity")
    private int quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderType type;

    @Column(name = "unitPrice")
    private double unitPrice;
    
    @Column(name = "currencyType")
    private String currencyType;
}

