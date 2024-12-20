package com.github.yuqingliu.economy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DepositCommand implements CommandExecutor {
    private final CurrencyService currencyService;
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("deposit") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            if (args.length == 2) {
                double amount = 0;
                try {
                    amount = Double.parseDouble(args[1]);
                } catch (Exception e) {
                    return false;
                }
                if(currencyService.depositPlayerPurse(player, args[0], amount)) {
                    logger.sendPlayerAcknowledgementMessage(player, String.format("Deposited %s %s!", args[1], args[0]));
                    return true;
                } else {
                    logger.sendPlayerErrorMessage(player, "Could not deposit that amount.");
                    return false;
                }
            }
            if (args.length == 3) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                double amount = 0;
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (Exception e) {
                    return false;
                }
                if(currencyService.depositPlayerPurse(target, args[1], amount)) {
                    logger.sendPlayerAcknowledgementMessage(player, String.format("Deposited %s %s to %s!", args[2], args[1], args[0]));
                    return true;
                } else {
                    logger.sendPlayerErrorMessage(player, "Could not deposit that amount.");
                    return false;
                }
            }
        }
        return false;
    }
}

