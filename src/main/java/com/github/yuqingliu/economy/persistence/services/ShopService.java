package com.github.yuqingliu.economy.persistence.services;

import java.util.List;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopSectionKey;
import com.github.yuqingliu.economy.persistence.repositories.ShopItemRepository;
import com.github.yuqingliu.economy.persistence.repositories.ShopOrderRepository;
import com.github.yuqingliu.economy.persistence.repositories.ShopRepository;
import com.github.yuqingliu.economy.persistence.repositories.ShopSectionRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ShopService {
    private final ShopRepository shopRepository;
    private final ShopSectionRepository shopSectionRepository;
    private final ShopItemRepository shopItemRepository;
    private final ShopOrderRepository shopOrderRepository;

    public boolean addShop(String shopName) {
        ShopEntity shop = new ShopEntity();
        shop.setShopName(shopName);
        return shopRepository.save(shop);
    }

    public boolean deleteShop(String shopName) {
        return shopRepository.delete(shopName);
    }

    public ShopEntity getShop(String shopName) {
        return shopRepository.get(shopName);
    }

    public boolean cancelBuyOrder(ShopOrderEntity order, Player player) {
        return shopOrderRepository.cancelBuyOrder(order, player);
    }

    public boolean claimBuyOrder(ShopOrderEntity order, Player player) {
        return shopOrderRepository.claimBuyOrder(order, player);
    }

    public boolean cancelSellOrder(ShopOrderEntity order, Player player) {
        return shopOrderRepository.cancelSellOrder(order, player);
    }

    public boolean claimSellOrder(ShopOrderEntity order, Player player) {
        return shopOrderRepository.claimSellOrder(order, player);
    }

    public boolean addShopSection(String shopName, String sectionName, ItemStack icon) {
        ShopSectionEntity section = new ShopSectionEntity();
        section.setShopName(shopName);
        section.setIcon(icon);
        section.setSectionName(sectionName);
        return shopSectionRepository.save(section);
    }

    public boolean deleteShopSection(String shopName, String sectionName) {
        ShopSectionKey key = new ShopSectionKey(sectionName, shopName);
        return shopSectionRepository.delete(key);
    }

    public boolean addShopItem(String shopName, String sectionName, ItemStack icon) {
        ShopItemEntity item = new ShopItemEntity();
        item.setIcon(icon);
        item.setItemName(PlainTextComponentSerializer.plainText().serialize(icon.displayName()));
        item.setShopName(shopName);
        item.setSectionName(sectionName);
        return shopItemRepository.save(item);
    }

    public ShopItemEntity getShopItem(String shopName, String sectionName, String itemName) {
        ShopItemKey key = new ShopItemKey(itemName, sectionName, shopName);
        return shopItemRepository.get(key);
    }

    public boolean deleteShopItem(String shopName, String sectionName, String itemName) {
        ShopItemKey key = new ShopItemKey(itemName, sectionName, shopName);
        return shopItemRepository.delete(key);
    }

    public boolean createBuyOrder(Player player, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        return shopOrderRepository.createBuyOrder(player, item, quantity, unitPrice, currencyType);
    }

    public boolean createSellOrder(Player player, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        return shopOrderRepository.createSellOrder(player, item, quantity, unitPrice, currencyType);
    }

    public boolean deleteOrder(ShopOrderEntity order) {
        return shopOrderRepository.delete(order.getOrderId());
    }

    public List<ShopOrderEntity> getPlayerBuyOrders(OfflinePlayer player) {
        return shopOrderRepository.getBuyOrdersByPlayer(player.getUniqueId());
    }

    public List<ShopOrderEntity> getPlayerSellOrders(OfflinePlayer player) {
        return shopOrderRepository.getSellOrdersByPlayer(player.getUniqueId());
    }

    public int[] quickBuy(ShopItemEntity item, int amount, String currencyType, Player player) {
        List<ShopOrderEntity> orders = item.getSellOrders().get(currencyType);
        int[] required = new int[]{amount, 0};
        for(ShopOrderEntity order : orders) {
            if(required[0] == 0) {
                return required;
            }
            required = shopOrderRepository.fillSellOrder(player, order, required, currencyType);
        }
        return required;
    }

    public int[] quickSell(ShopItemEntity item, int amount, String currencyType, Player player) {
        List<ShopOrderEntity> orders = item.getBuyOrders().get(currencyType);
        int[] required = new int[]{amount, 0};
        for(ShopOrderEntity order : orders) {
            if(required[0] == 0) {
                return required;
            }
            required = shopOrderRepository.fillBuyOrder(player, order, required, currencyType);
        }
        return required;
    }
}

