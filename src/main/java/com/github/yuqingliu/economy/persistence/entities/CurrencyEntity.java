package com.github.yuqingliu.economy.persistence.entities;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.keys.CurrencyKey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "currencies")
@IdClass(CurrencyKey.class)
@Getter
@Setter
@NoArgsConstructor
public class CurrencyEntity {
    
    @Id
    @Column(name = "currencyName", columnDefinition = "VARCHAR(16)")
    private String currencyName;

    @Id
    @Column(name = "accountId", columnDefinition = "VARCHAR(36)")
    private UUID accountId;

    @Id
    @Column(name = "purseId", columnDefinition = "VARCHAR(36)")
    private UUID purseId;

    @Column(name = "amount")
    private double amount;

    @Column(name = "icon", columnDefinition = "BLOB")
    private byte[] icon;

    @ManyToOne
    @JoinColumn(name = "accountId", insertable = false, updatable = false)
    private AccountEntity account;

    @ManyToOne
    @JoinColumn(name = "purseId", insertable = false, updatable = false)
    private PurseEntity purse;

    public ItemStack getIcon() {
        return ItemStack.deserializeBytes(this.icon);
    }

    public void setIcon(ItemStack itemStack) {
        this.icon = itemStack.serializeAsBytes();
    }
}

