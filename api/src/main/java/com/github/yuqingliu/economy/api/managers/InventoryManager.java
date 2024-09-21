package com.github.yuqingliu.economy.api.managers;

import com.github.yuqingliu.economy.api.view.PlayerInventory;

public interface InventoryManager {
    PlayerInventory getInventory(String className);
}
