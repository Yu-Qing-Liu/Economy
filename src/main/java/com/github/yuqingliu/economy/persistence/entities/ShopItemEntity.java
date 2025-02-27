package com.github.yuqingliu.economy.persistence.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        @JoinColumn(name = "sectionName", referencedColumnName = "sectionName", insertable = false, updatable = false),
        @JoinColumn(name = "shopName", referencedColumnName = "shopName", insertable = false, updatable = false)
    })
    private ShopSectionEntity shopSection;

    @OneToMany(mappedBy = "shopItem", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("itemName ASC")
    private List<ShopOrderEntity> orders = new ArrayList<>();

    @Transient
    private Map<String, List<ShopOrderEntity>> buyOrders = new ConcurrentHashMap<>();

    @Transient
    private Map<String, List<ShopOrderEntity>> sellOrders = new ConcurrentHashMap<>();

    public Map<String, List<ShopOrderEntity>> getBuyOrders() {
        buyOrders.clear();
        for (ShopOrderEntity order : orders) {
            if (order.getType() == ShopOrderEntity.OrderType.BUY) {
                buyOrders.computeIfAbsent(order.getCurrencyType(), k -> new ArrayList<>()).add(order);
                buyOrders.get(order.getCurrencyType()).sort(Comparator.comparingDouble(ShopOrderEntity::getUnitPrice).reversed().thenComparingInt(o -> -orders.indexOf(o)));
            }
        }
        return buyOrders;
    }

    public Map<String, List<ShopOrderEntity>> getSellOrders() {
        sellOrders.clear();
        for (ShopOrderEntity order : orders) {
            if (order.getType() == ShopOrderEntity.OrderType.SELL) {
                sellOrders.computeIfAbsent(order.getCurrencyType(), k -> new ArrayList<>()).add(order);
                sellOrders.get(order.getCurrencyType()).sort(Comparator.comparingDouble(ShopOrderEntity::getUnitPrice).thenComparingInt(o -> -orders.indexOf(o)));
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
