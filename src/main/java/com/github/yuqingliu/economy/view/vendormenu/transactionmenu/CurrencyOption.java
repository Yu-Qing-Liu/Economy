package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor
@Getter
public class CurrencyOption {
    private final ItemStack icon;
    private final double buyPrice;
    private final double sellPrice;

    public double getBuyPrice(int amount) {
        return buyPrice * amount;
    }

    public double getSellPrice(int amount) {
        return sellPrice * amount;
    }

    public String getCurrencyName() {
        String name = PlainTextComponentSerializer.plainText().serialize(icon.displayName());
        return name.length() > 1 ? name.substring(1, name.length() - 1) : name;
    }
}
