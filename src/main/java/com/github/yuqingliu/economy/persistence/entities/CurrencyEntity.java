package com.github.yuqingliu.economy.persistence.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

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
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(this.icon);
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
            ItemStack item = (ItemStack) bois.readObject();
            bais.close();
            bois.close();
            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setIcon(ItemStack itemStack) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
            boos.writeObject(itemStack);
            this.icon = baos.toByteArray();
            boos.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

