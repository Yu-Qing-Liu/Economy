package com.github.yuqingliu.economy.logger;

import java.time.Duration;

import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.api.logger.Logger;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@NoArgsConstructor
public class LoggerImpl implements Logger {
    @Override
    public void sendPlayerErrorMessage(Player player, String message) {
        player.sendMessage(Component.text(message, NamedTextColor.RED));
    } 
    
    @Override
    public void sendPlayerWarningMessage(Player player, String message) {
        player.sendMessage(Component.text(message, NamedTextColor.YELLOW));
    }

    @Override
    public void sendPlayerAcknowledgementMessage(Player player, String message) {
        player.sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    @Override
    public void sendPlayerNotificationMessage(Player player, String message) {
        player.sendMessage(Component.text(message, NamedTextColor.GOLD));
    }

    @Override
    public void sendPlayerMessage(Player player, String message, NamedTextColor color) {
        player.sendMessage(Component.text(message, color));
    }

    @Override
    public String durationToString(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d days: %02d hrs: %02d mins: %02d s", days, hours, minutes, seconds);
    }
}
