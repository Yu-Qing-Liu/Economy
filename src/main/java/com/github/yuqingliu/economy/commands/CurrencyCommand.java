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
public class CurrencyCommand implements CommandExecutor {
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
                return false;
            }
            switch (args[0]) {
                case "add":
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
                    return currencyService.addCurrencyToAll(args[1], 0, icon);
                case "remove":
                    return currencyService.deleteCurrencyFromAll(args[1]);
                default:
                    break;
            }
            return true;
        }
        return false;
    }
}

