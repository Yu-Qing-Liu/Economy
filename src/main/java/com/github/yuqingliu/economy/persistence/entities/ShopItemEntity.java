package com.github.yuqingliu.economy.persistence.entities;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.keys.ShopItemKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shopitems")
@IdClass(ShopItemKey.class)
@Getter
@Setter
@NoArgsConstructor
public class ShopItemEntity {
    
    @Id
    @Column(name = "itemName", columnDefinition = "VARCHAR(16)")
    private String itemName;

    @Id
    @Column(name = "sectionName", columnDefinition = "VARCHAR(16)")
    private String sectionName;

    @Id
    @Column(name = "shopName", columnDefinition = "VARCHAR(16)")
    private String shopName;

    @Column(name = "icon", columnDefinition = "BLOB")
    private byte[] icon;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "shopName", referencedColumnName = "shopName", insertable = false, updatable = false),
        @JoinColumn(name = "sectionName", referencedColumnName = "sectionName", insertable = false, updatable = false)
    })
    private ShopSectionEntity shopSection;

    @OneToMany(mappedBy = "shopItem", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ShopOrderEntity> orders = new HashSet<>();

    private TreeSet<ShopOrderEntity> buyOrders = new TreeSet<>(Comparator.comparingDouble(ShopOrderEntity::getUnitPrice).reversed());
    private TreeSet<ShopOrderEntity> sellOrders = new TreeSet<>(Comparator.comparingDouble(ShopOrderEntity::getUnitPrice));

    public TreeSet<ShopOrderEntity> getBuyOrders() {
        for (ShopOrderEntity order : orders) {
            if (order.getType() == ShopOrderEntity.OrderType.BUY) {
                buyOrders.add(order);
            } 
        }
        return buyOrders;
    }

    public TreeSet<ShopOrderEntity> getSellOrders() {
        for (ShopOrderEntity order : orders) {
            if (order.getType() == ShopOrderEntity.OrderType.SELL) {
                sellOrders.add(order);
            } 
        }
        return sellOrders;
    }

    public ItemStack getIcon() {
        return ItemStack.deserializeBytes(this.icon);
    }

    public void setIcon(ItemStack itemStack) {
        this.icon = itemStack.serializeAsBytes();
    }
}
