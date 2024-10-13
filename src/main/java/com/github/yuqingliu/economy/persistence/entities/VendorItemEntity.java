package com.github.yuqingliu.economy.persistence.entities;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.keys.VendorItemKey;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vendoritems")
@IdClass(VendorItemKey.class)
@Getter
@Setter
@NoArgsConstructor
public class VendorItemEntity {
    
    @Id
    @Column(name = "itemName", columnDefinition = "VARCHAR(16)")
    private String itemName;

    @Id
    @Column(name = "sectionName", columnDefinition = "VARCHAR(16)")
    private String sectionName;

    @Id
    @Column(name = "vendorName", columnDefinition = "VARCHAR(16)")
    private String vendorName;
    
    @Column(name = "icon", columnDefinition = "BLOB")
    private byte[] icon;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "vendorName", referencedColumnName = "vendorName", insertable = false, updatable = false),
        @JoinColumn(name = "sectionName", referencedColumnName = "sectionName", insertable = false, updatable = false)
    })
    private VendorSectionEntity vendorSection;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "buy_prices",
        joinColumns = {
            @JoinColumn(name = "itemName", referencedColumnName = "itemName"),
            @JoinColumn(name = "sectionName", referencedColumnName = "sectionName"),
            @JoinColumn(name = "vendorName", referencedColumnName = "vendorName")
        }
    )
    @MapKeyColumn(name = "currency")
    @Column(name = "price")
    private Map<String, Double> buyPrices = new LinkedHashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "sell_prices",
        joinColumns = {
            @JoinColumn(name = "itemName", referencedColumnName = "itemName"),
            @JoinColumn(name = "sectionName", referencedColumnName = "sectionName"),
            @JoinColumn(name = "vendorName", referencedColumnName = "vendorName")
        }
    )
    @MapKeyColumn(name = "currency")
    @Column(name = "price")
    private Map<String, Double> sellPrices = new LinkedHashMap<>();

    public ItemStack getIcon() {
        return ItemStack.deserializeBytes(this.icon);
    }

    public void setIcon(ItemStack itemStack) {
        this.icon = itemStack.serializeAsBytes();
    }
}
