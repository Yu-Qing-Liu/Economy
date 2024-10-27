package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.services.BankService;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@RequiredArgsConstructor
public class AccountCommand implements CommandExecutor {
    @Inject
    private final BankService bankService;
    @Inject
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("account") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            if (args.length == 6) {
                if(args[0].equals("create")) {
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid section icon. Please have a valid item in main hand.");
                        return false;
                    }
                    icon.setAmount(1);
                    ItemMeta meta = icon.getItemMeta();
                    if(meta != null) {
                        meta.displayName(Component.text(args[1], NamedTextColor.RED).decorate(TextDecoration.BOLD));
                        icon.setItemMeta(meta);
                    }
                    double interestRate = Double.parseDouble(args[3]);
                    double cost = Double.parseDouble(args[5]);
                    if(bankService.addBankAccountToAll(args[1], args[2], icon, interestRate, args[4], cost)) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Sucessfully create account with name %s in bank %s with an interest rate of %s%%. Unlocking the account costs %s %s.", args[1], args[2], args[3], args[5], args[4]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Could not create account. Please ensure correct parameters.");
                        return false;
                    }
                }
            }
            if (args.length == 3) {
                if(args[0].equals("delete")) {
                    if(bankService.deleteBankAccountFromAll(args[1], args[2])) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Sucessfully deleted account %s in bank %s", args[1], args[2]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Could not delete account.");
                        return false;
                    }
                }
            }
        }
        return false;
    }
}

