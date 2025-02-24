package com.github.yuqingliu.economy.api.logger;

import java.time.Duration;

import org.bukkit.entity.Player;
import net.kyori.adventure.text.format.NamedTextColor;

public interface Logger {
    void sendPlayerErrorMessage(Player player, String message);
    void sendPlayerWarningMessage(Player player, String message);
    void sendPlayerAcknowledgementMessage(Player player, String message);
    void sendPlayerNotificationMessage(Player player, String message);
    void sendPlayerMessage(Player player, String message, NamedTextColor color);
    String durationToString(Duration duration);
}
