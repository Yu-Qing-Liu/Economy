package com.github.yuqingliu.economy.persistence.services;

import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.entities.VendorEntity;
import com.github.yuqingliu.economy.persistence.entities.VendorSectionEntity;
import com.github.yuqingliu.economy.persistence.entities.keys.VendorSectionKey;
import com.github.yuqingliu.economy.persistence.repositories.VendorRepository;
import com.github.yuqingliu.economy.persistence.repositories.VendorSectionRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class VendorService {
    @Inject
    private final VendorRepository vendorRepository;
    @Inject
    private final VendorSectionRepository vendorSectionRepository;

    public void addVendor(String vendorName) {
        VendorEntity vendor = new VendorEntity();
        vendor.setVendorName(vendorName);
        vendorRepository.save(vendor);
    }

    public void deleteVendor(String vendorName) {
        vendorRepository.delete(vendorName);
    }

    public VendorEntity getVendor(String vendorName) {
        return vendorRepository.get(vendorName);
    }

    public void addVendorSection(VendorEntity vendor, String sectionName, ItemStack icon) {
        VendorSectionEntity section = new VendorSectionEntity();
        section.setVendorName(vendor.getVendorName());
        section.setIcon(icon);
        section.setSectionName(sectionName);
        section.setVendor(vendor);
        vendorSectionRepository.save(section);
    }

    public void deleteVendorSection(String vendorName, String sectionName) {
        VendorSectionKey key = new VendorSectionKey(sectionName, vendorName);
        vendorSectionRepository.delete(key);
    }
}

