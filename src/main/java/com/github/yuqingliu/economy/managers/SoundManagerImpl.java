package com.github.yuqingliu.economy.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.api.managers.SoundManager;
import com.google.inject.Singleton;

@Singleton
public class SoundManagerImpl implements SoundManager {
    @Override
    public void playTransactionSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
    }

    @Override
    public void playConfirmOrderSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }
}
