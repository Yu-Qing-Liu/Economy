package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CurrencyCommand implements CommandExecutor {
    private final CurrencyService currencyService;
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("currency") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            if (args.length != 2) {
                return false;
            }
            switch (args[0]) {
                case "add":
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid section icon. Please have a valid item in main hand.");
                        return false;
                    }
                    icon.setAmount(1);
                    ItemMeta meta = icon.getItemMeta();
                    if(meta != null) {
                        meta.displayName(Component.text(args[1], NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                        icon.setItemMeta(meta);
                    }
                    if(currencyService.addCurrencyToAll(args[1], 0, icon)) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Added %s currency to all!", args[1]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, String.format("Could not add %s currency", args[1]));
                        return false;
                    }
                case "remove":
                    if(currencyService.deleteCurrencyFromAll(args[1])) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Removed %s currency from all!", args[1]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, String.format("Could not remove %s currency", args[1]));
                        return false;
                    }
                default:
                    return false;
            }
        }
        return false;
    }
}

