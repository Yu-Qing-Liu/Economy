package com.github.yuqingliu.economy.persistence.services;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.ShopEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity.OrderType;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.ShopOrderKey;
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
@RequiredArgsConstructor
public class ShopService {
    @Inject
    private final ShopRepository shopRepository;
    @Inject
    private final ShopSectionRepository shopSectionRepository;
    @Inject
    private final ShopItemRepository shopItemRepository;
    @Inject
    private final ShopOrderRepository shopOrderRepository;
    @Inject 
    private final CurrencyService currencyService;

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

    public boolean deleteShopItem(String shopName, String sectionName, String itemName) {
        ShopItemKey key = new ShopItemKey(itemName, sectionName, shopName);
        return shopItemRepository.delete(key);
    }

    public boolean createBuyOrder(OfflinePlayer player, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        ShopOrderEntity order = new ShopOrderEntity();
        order.setType(OrderType.BUY);
        order.setPlayerId(player.getUniqueId());
        order.setItemName(item.getItemName());
        order.setSectionName(item.getSectionName());
        order.setShopName(item.getShopName());
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setCurrencyType(currencyType);
        item.getOrders().add(order);
        return shopItemRepository.update(item);
    }

    public boolean createSellOrder(OfflinePlayer player, ShopItemEntity item, int quantity, double unitPrice, String currencyType) {
        ShopOrderEntity order = new ShopOrderEntity();
        order.setType(OrderType.SELL);
        order.setPlayerId(player.getUniqueId());
        order.setItemName(item.getItemName());
        order.setSectionName(item.getSectionName());
        order.setShopName(item.getShopName());
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setCurrencyType(currencyType);
        item.getOrders().add(order);
        return shopItemRepository.update(item);
    }

    public boolean updateOrder(ShopOrderEntity newOrder) {
        return shopOrderRepository.update(newOrder);
    }

    public boolean deleteOrder(ShopOrderEntity order) {
        ShopOrderKey key = new ShopOrderKey(order.getPlayerId(), order.getItemName(), order.getSectionName(), order.getShopName(), order.getType(), order.getCurrencyType());
        return shopOrderRepository.delete(key);
    }

    public List<ShopOrderEntity> getPlayerBuyOrders(OfflinePlayer player) {
        return shopOrderRepository.getBuyOrdersByPlayer(player.getUniqueId());
    }

    public List<ShopOrderEntity> getPlayerSellOrders(OfflinePlayer player) {
        return shopOrderRepository.getSellOrdersByPlayer(player.getUniqueId());
    }
}

