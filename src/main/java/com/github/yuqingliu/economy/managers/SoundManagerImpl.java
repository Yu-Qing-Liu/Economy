package com.github.yuqingliu.economy.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.api.managers.SoundManager;

public class SoundManagerImpl implements SoundManager {
    @Override
    public void playTransactionSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
    }
}
