package com.github.yuqingliu.economy.view.shopmenu.ordersmenu;

import java.time.Duration;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;

@Getter
public class OrdersMenuController {
    private final ShopMenu shopMenu;
    private Material voidOption = Material.GLASS_PANE;
    
    public OrdersMenuController(ShopMenu shopMenu) {
        this.shopMenu = shopMenu;
    }

    public void openOrdersMenu(Inventory inv, Player player) {
        Scheduler.runLaterAsync((task) -> {
            shopMenu.getPlayerMenuTypes().put(player, MenuType.OrdersMenu);
        }, Duration.ofMillis(50));
        shopMenu.clear(inv);
    }

    public void onClose(Player player) {

    }
}
