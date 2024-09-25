package com.github.yuqingliu.economy.persistence.entities;

import java.util.HashSet;
import java.util.Set;

import com.github.yuqingliu.economy.persistence.entities.keys.ShopSectionKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shopsections")
@IdClass(ShopSectionKey.class)
@Getter
@Setter
@NoArgsConstructor
public class ShopSectionEntity {
    @Id
    @Column(name = "sectionName", columnDefinition = "VARCHAR(16)")
    private String sectionName;
    
    @Id
    @Column(name = "shopName", columnDefinition = "VARCHAR(16)")
    private String shopName;

    @ManyToOne
    @JoinColumn(name = "shopName", insertable = false, updatable = false)
    private ShopEntity shop;

    @OneToMany(mappedBy = "shopSection", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ShopItemEntity> items = new HashSet<>();

    public ShopSectionEntity(String sectionName, ShopEntity shop) {
        this.sectionName = sectionName;
        this.shop = shop;
    } 
}
