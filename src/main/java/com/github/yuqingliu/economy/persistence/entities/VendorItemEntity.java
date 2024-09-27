package com.github.yuqingliu.economy.persistence.entities;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.keys.VendorItemKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "buyPrice")
    private double buyPrice;

    @Column(name = "sellPrice")
    private double sellPrice;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CurrencyEntity currencyType;

    public ItemStack getIcon() {
        return ItemStack.deserializeBytes(this.icon);
    }

    public void setIcon(ItemStack itemStack) {
        this.icon = itemStack.serializeAsBytes();
    }
}
