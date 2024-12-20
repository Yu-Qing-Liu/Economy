package com.github.yuqingliu.economy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuctionHouseCommand implements CommandExecutor {
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("ah") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            switch (args.length) {
                case 0 -> {
                    // Open GUI
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }
        return false;
    }
}
