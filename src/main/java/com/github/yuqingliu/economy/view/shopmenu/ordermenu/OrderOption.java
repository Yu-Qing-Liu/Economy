package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor
@Getter
public class OrderOption {
    private final ItemStack icon;
    private final Set<ShopOrderEntity> orders;

    public String getCurrencyName() {
        String name = PlainTextComponentSerializer.plainText().serialize(icon.displayName());
        return name.length() > 1 ? name.substring(1, name.length() - 1) : name;
    }
}
