package com.github.yuqingliu.economy.persistence.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "currencies")
@Getter
@Setter
@RequiredArgsConstructor
public class CurrencyEntity {
    @Id
    @Column(name = "currencyName", columnDefinition = "VARCHAR(16)")
    private String id;

    @Column(name = "amount")
    private double amount;

    @Column(name = "icon", columnDefinition = "BLOB")
    private byte[] icon;

    @ManyToOne
    @JoinColumn(name = "accountId")
    private AccountEntity account;

    @ManyToOne
    @JoinColumn(name = "purseId")
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
