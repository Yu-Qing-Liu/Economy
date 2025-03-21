package com.github.yuqingliu.economy.view.shopmenu.ordermenu;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor
@Getter
public class OrderOption {
    private final ItemStack icon;
    private final List<ShopOrderEntity> orders;

    public String getCurrencyName() {
        String name = PlainTextComponentSerializer.plainText().serialize(icon.displayName());
        return name.replaceAll("[\\[\\]]", "");
    }
}
