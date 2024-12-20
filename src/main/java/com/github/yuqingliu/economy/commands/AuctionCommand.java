package com.github.yuqingliu.economy.commands;

import java.time.Duration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.services.AuctionService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuctionCommand implements CommandExecutor {
    private final Logger logger;
    private final AuctionService auctionService;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("ah") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            switch (args.length) {
                case 4 -> {
                    if(!args[1].equals("create")) {
                        return false;
                    }
                    try {
                        double startingBid = Double.parseDouble(args[2]);
                        String currencyType = args[3];
                        int length = Integer.parseInt(args[4]);
                        Duration duration = Duration.ofMinutes(length);
                        ItemStack item = player.getInventory().getItemInMainHand();
                        auctionService.startAuction(player, item, startingBid, currencyType, duration);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
                case 5 -> {
                    if(!args[1].equals("create")) {
                        return false;
                    }
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

