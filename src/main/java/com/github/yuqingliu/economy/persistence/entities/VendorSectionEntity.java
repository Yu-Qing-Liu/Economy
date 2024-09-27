package com.github.yuqingliu.economy.persistence.entities;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.keys.VendorSectionKey;

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
@Table(name = "vendorsections")
@IdClass(VendorSectionKey.class)
@Getter
@Setter
@NoArgsConstructor
public class VendorSectionEntity {
    @Id
    @Column(name = "sectionName", columnDefinition = "VARCHAR(16)")
    private String sectionName;
    
    @Id
    @Column(name = "vendorName", columnDefinition = "VARCHAR(16)")
    private String vendorName;

    @Column(name = "icon", columnDefinition = "BLOB")
    private byte[] icon;

    @ManyToOne
    @JoinColumn(name = "vendorName", insertable = false, updatable = false)
    private VendorEntity vendor;

    @OneToMany(mappedBy = "vendorSection", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VendorItemEntity> items = new HashSet<>();

    public ItemStack getIcon() {
        return ItemStack.deserializeBytes(this.icon);
    }

    public void setIcon(ItemStack itemStack) {
        this.icon = itemStack.serializeAsBytes();
    }
}
