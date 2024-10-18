package com.github.yuqingliu.economy.view.shopmenu.ordersmenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PlayerSellOrdersData {
    private Map<Integer, ShopOrderEntity[]> sellOrdersPageData = new ConcurrentHashMap<>();
    private int[] pageNumber = new int[]{1};
}
