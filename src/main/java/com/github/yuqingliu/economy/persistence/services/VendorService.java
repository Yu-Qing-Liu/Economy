package com.github.yuqingliu.economy.persistence.services;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.VendorEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorSectionKey;
import com.github.yuqingliu.economy.persistence.repositories.VendorItemRepository;
import com.github.yuqingliu.economy.persistence.repositories.VendorRepository;
import com.github.yuqingliu.economy.persistence.repositories.VendorSectionRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VendorService {
    private final VendorRepository vendorRepository;
    private final VendorSectionRepository vendorSectionRepository;
    private final VendorItemRepository vendorItemRepository;

    public boolean addVendor(String vendorName) {
        VendorEntity vendor = new VendorEntity();
        vendor.setVendorName(vendorName);
        return vendorRepository.save(vendor);
    }

    public boolean deleteVendor(String vendorName) {
        return vendorRepository.delete(vendorName);
    }

    public VendorEntity getVendor(String vendorName) {
        return vendorRepository.get(vendorName);
    }

    public boolean addVendorSection(String vendorName, String sectionName, ItemStack icon) {
        VendorSectionEntity section = new VendorSectionEntity();
        section.setVendorName(vendorName);
        section.setIcon(icon);
        section.setSectionName(sectionName);
        return vendorSectionRepository.save(section);
    }

    public boolean deleteVendorSection(String vendorName, String sectionName) {
        VendorSectionKey key = new VendorSectionKey(sectionName, vendorName);
        return vendorSectionRepository.delete(key);
    }

    public boolean addVendorItem(String vendorName, String sectionName, ItemStack icon, Map<String, Double> buyPrices, Map<String, Double> sellPrices) {
        if(!buyPrices.isEmpty() && !sellPrices.isEmpty()) {
            VendorItemEntity item = new VendorItemEntity();
            item.setIcon(icon);
            item.setBuyPrices(buyPrices);
            item.setItemName(PlainTextComponentSerializer.plainText().serialize(icon.displayName()));
            item.setSellPrices(sellPrices);
            item.setVendorName(vendorName);
            item.setSectionName(sectionName);
            return vendorItemRepository.save(item);
        }
        return false;
    }

    public boolean updateVendorItem(String vendorName, String sectionName, ItemStack icon, Map<String, Double> buyPrices, Map<String, Double> sellPrices) {
        String itemName = PlainTextComponentSerializer.plainText().serialize(icon.displayName());
        VendorItemEntity item = vendorItemRepository.get(new VendorItemKey(itemName, sectionName, vendorName));
        if(item == null) {
            return false;
        }
        item.getBuyPrices().putAll(buyPrices);
        item.getSellPrices().putAll(sellPrices);
        return vendorItemRepository.update(item);
    }

    public boolean deleteVendorItem(String vendorName, String sectionName, String itemName) {
        VendorItemKey key = new VendorItemKey(itemName, sectionName, vendorName);
        return vendorItemRepository.delete(key);
    }
}

