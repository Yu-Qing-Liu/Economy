package com.github.yuqingliu.economy.persistence.entities;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
    @OrderBy("itemName ASC")
    private Set<ShopOrderEntity> orders = new HashSet<>();
    
    @Transient
    private Map<String, Set<ShopOrderEntity>> buyOrders = new ConcurrentHashMap<>(); 

    @Transient
    private Map<String, Set<ShopOrderEntity>> sellOrders = new ConcurrentHashMap<>();

    public Map<String, Set<ShopOrderEntity>> getBuyOrders() {
        buyOrders.clear();
        for (ShopOrderEntity order : orders) {
            if (order.getType() == ShopOrderEntity.OrderType.BUY) {
                if(buyOrders.containsKey(order.getCurrencyType())) {
                    buyOrders.get(order.getCurrencyType()).add(order);
                } else {
                    Set<ShopOrderEntity> set = new TreeSet<>(Comparator.comparingDouble(ShopOrderEntity::getUnitPrice).reversed());
                    set.add(order);
                    buyOrders.put(order.getCurrencyType(), set);
                }
            } 
        }
        return buyOrders;
    }

    public Map<String, Set<ShopOrderEntity>> getSellOrders() {
        sellOrders.clear();
        for (ShopOrderEntity order : orders) {
            if (order.getType() == ShopOrderEntity.OrderType.SELL) {
                if(sellOrders.containsKey(order.getCurrencyType())) {
                    sellOrders.get(order.getCurrencyType()).add(order);
                } else {
                    Set<ShopOrderEntity> set = new TreeSet<>(Comparator.comparingDouble(ShopOrderEntity::getUnitPrice));
                    set.add(order);
                    sellOrders.put(order.getCurrencyType(), set);
                }
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
