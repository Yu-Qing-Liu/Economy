package com.github.yuqingliu.economy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WithdrawCommand implements CommandExecutor {
    @Inject
    private final CurrencyService currencyService;
    @Inject
    private Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("withdraw") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            if (args.length != 2) {
                return false;
            }
            if(currencyService.withdrawPlayerPurse(player, args[0], Double.parseDouble(args[1]))) {
                logger.sendPlayerAcknowledgementMessage(player, String.format("Sucessful withdrawal of %s %s", args[1], args[0]));
                return true;
            } else {
                logger.sendPlayerErrorMessage(player, "Could not withdraw that amount.");
                return false;
            }
        }
        return false;
    }
}

