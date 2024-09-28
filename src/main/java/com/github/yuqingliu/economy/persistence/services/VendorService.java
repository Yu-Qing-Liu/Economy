package com.github.yuqingliu.economy.persistence.services;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorItemKey;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorSectionKey;
import com.github.yuqingliu.economy.persistence.repositories.CurrencyRepository;
import com.github.yuqingliu.economy.persistence.repositories.VendorItemRepository;
import com.github.yuqingliu.economy.persistence.repositories.VendorRepository;
import com.github.yuqingliu.economy.persistence.repositories.VendorSectionRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Singleton
@RequiredArgsConstructor
public class VendorService {
    @Inject
    private final VendorRepository vendorRepository;
    @Inject
    private final VendorSectionRepository vendorSectionRepository;
    @Inject
    private final VendorItemRepository vendorItemRepository;
    @Inject
    private final CurrencyRepository currencyRepository;

    public boolean addVendor(String vendorName) {
        VendorEntity vendor = new VendorEntity();
        vendor.setVendorName(vendorName);
        return vendorRepository.save(vendor);
    }

    public void deleteVendor(String vendorName) {
        vendorRepository.delete(vendorName);
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

    public void deleteVendorSection(String vendorName, String sectionName) {
        VendorSectionKey key = new VendorSectionKey(sectionName, vendorName);
        vendorSectionRepository.delete(key);
    }

    public boolean addVendorItem(String vendorName, String sectionName, ItemStack icon, String currencyName, double buyPrice, double sellPrice) {
        CurrencyEntity temp = currencyRepository.getFirst(currencyName);
        if(temp != null) {
            VendorItemEntity item = new VendorItemEntity();
            item.setIcon(icon);
            item.setBuyPrice(buyPrice);
            item.setItemName(PlainTextComponentSerializer.plainText().serialize(icon.displayName()));
            item.setSellPrice(sellPrice);
            item.setVendorName(vendorName);
            item.setSectionName(sectionName);
            item.setCurrencyType(temp.getCurrencyName());
            return vendorItemRepository.save(item);
        }
        return false;
    }

    public void deleteVendorItem(String vendorName, String sectionName, String itemName) {
        VendorItemKey key = new VendorItemKey(itemName, sectionName, vendorName);
        vendorItemRepository.delete(key);
    }
}

