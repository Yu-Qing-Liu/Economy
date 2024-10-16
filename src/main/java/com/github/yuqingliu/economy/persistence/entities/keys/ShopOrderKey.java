package com.github.yuqingliu.economy.persistence.entities.keys;

import java.io.Serializable;
import java.util.UUID;

import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity.OrderType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class ShopOrderKey implements Serializable {
    private UUID playerId;
    private String itemName;
    private String sectionName;
    private String shopName;
    private OrderType type;
    private String currencyType;
}
