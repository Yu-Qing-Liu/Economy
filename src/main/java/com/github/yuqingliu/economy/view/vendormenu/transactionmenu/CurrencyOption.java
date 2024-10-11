package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CurrencyOption {
    private final ItemStack icon;
    private final double buyPrice;
    private final double sellPrice;
}
