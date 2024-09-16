package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@RequiredArgsConstructor
public class Currency implements CommandExecutor {
    @Inject
    private final CurrencyService currencyService;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("currency") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return false;
            }
            if (args.length != 2) {
                player.sendMessage(Component.text("Usage: /currency add <name>; /currency remove <name>", NamedTextColor.RED));
                return false;
            }
            switch (args[0]) {
                case "add":
                    try {
                        ItemStack icon = player.getInventory().getItemInMainHand().clone();
                        if(icon.getType() == Material.AIR) {
                            player.sendMessage(Component.text("Invalid section icon. Please have a valid item in main hand.", NamedTextColor.RED));
                            return false;
                        }
                        icon.setAmount(1);
                        ItemMeta meta = icon.getItemMeta();
                        if(meta != null) {
                            meta.displayName(Component.text(args[1], NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                            icon.setItemMeta(meta);
                        }
                        currencyService.addCurrencyToAll(args[1], 0, icon);
                        player.sendMessage(Component.text("Successfully added a new currency named " + args[1], NamedTextColor.GREEN));
                    } catch (Exception e) {
                        player.sendMessage(Component.text("Currency names must be unique", NamedTextColor.RED));
                        return false;                   
                    }
                    break;
                case "remove":
                    try {
                        //new RemoveCurrency(args[1]).update();
                        player.sendMessage(Component.text("Successfully removed currency named " + args[1], NamedTextColor.RED));
                    } catch (Exception e) {
                        player.sendMessage(Component.text("An internal error occured.", NamedTextColor.RED));
                        return false;
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
        return false;
    }
}

