package com.github.yuqingliu.economy.view.shopmenu.buyordermenu;

import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlayerData {
    private ItemStack currencyTypeIcon;
    private ItemStack quantityIcon;
    private ItemStack unitPriceIcon;
    private ItemStack orderIcon;
    private String currencyTypeInput;
    private int quantityInput;
    private double unitPriceInput;
}
