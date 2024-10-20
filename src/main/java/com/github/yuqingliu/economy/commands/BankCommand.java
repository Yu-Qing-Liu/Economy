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
            if (args.length != 2) {
                return false;
            }
            switch (args[0]) {
                case "create":
                    if(bankService.addBank(args[1])) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Successfully created bank with name %s.", args[1]));
                    } else {
                        logger.sendPlayerWarningMessage(player, "Bank already exists. Providing another entrypoint.");
                    } 
                    Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                    PersistentDataContainer container = villager.getPersistentDataContainer();
                    container.set(nameSpacedKeyManager.getBankKey(), PersistentDataType.STRING, args[1]);
                    villager.setPersistent(true);
                    return true;
                case "delete":
                    if(bankService.deleteBank(args[1])) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Bank with name %s has been deleted", args[1]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Bank could not be deleted.");
                        return false;
                    }
                default:
                    return false;
            }
        }
        return false;
    }
}
