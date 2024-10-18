package com.github.yuqingliu.economy.api.managers;

import org.bukkit.entity.Player;

public interface SoundManager {
    void playTransactionSound(Player player);
    void playConfirmOrderSound(Player player);
}
