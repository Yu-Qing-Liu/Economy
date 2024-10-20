package com.github.yuqingliu.economy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.github.yuqingliu.economy.persistence.services.BankService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BankCommand implements CommandExecutor {
    @Inject
    private final NameSpacedKeyManager nameSpacedKeyManager;
    @Inject
    private final BankService bankService;
    @Inject
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("bank") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            if (args.length == 3) {
                if(args[0].equals("create")) {
                    long cooldown = Long.parseLong(args[2]);
                    if(bankService.addBank(args[1], cooldown)) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Successfully created bank with name %s with an interest deposit every %s hours", args[1], args[2]));
                    } else {
                        logger.sendPlayerWarningMessage(player, "Bank already exists. Providing another entrypoint.");
                    } 
                    Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                    PersistentDataContainer container = villager.getPersistentDataContainer();
                    container.set(nameSpacedKeyManager.getBankKey(), PersistentDataType.STRING, args[1]);
                    villager.setPersistent(true);
                    return true;
                }
            }
            if (args.length == 2) {
                if(args[0].equals("delete")) {
                    if(bankService.deleteBank(args[1])) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Bank with name %s has been deleted", args[1]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Bank could not be deleted.");
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
