package com.github.yuqingliu.economy.persistence.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shoporders")
@Getter
@Setter
@NoArgsConstructor
public class ShopOrderEntity {
    public enum OrderType {
        BUY, SELL;
    }
    @Id
    @GeneratedValue
    @Column(name = "orderId", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID orderId;

    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private UUID playerId;
    
    @Column(name = "itemName", columnDefinition = "VARCHAR(16)")
    private String itemName;

    @Column(name = "sectionName", columnDefinition = "VARCHAR(16)")
    private String sectionName;

    @Column(name = "shopName", columnDefinition = "VARCHAR(16)")
    private String shopName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderType type;
    
    @Column(name = "currencyType")
    private String currencyType;

    @ManyToOne
    @JoinColumn(name = "playerId", insertable = false, updatable = false)
    private PlayerEntity player;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "sectionName", referencedColumnName = "sectionName", insertable = false, updatable = false),
        @JoinColumn(name = "shopName", referencedColumnName = "shopName", insertable = false, updatable = false),
        @JoinColumn(name = "itemName", referencedColumnName = "itemName", insertable = false, updatable = false)
    })
    private ShopItemEntity shopItem;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "unitPrice")
    private double unitPrice;
    

    @Column(name = "filledQantity")
    private int filledQuantity = 0;
}

