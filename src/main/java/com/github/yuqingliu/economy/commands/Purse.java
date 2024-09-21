package com.github.yuqingliu.economy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.yuqingliu.economy.api.managers.InventoryManager;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@RequiredArgsConstructor
public class Purse implements CommandExecutor {
    @Inject
    private final InventoryManager inventoryManager;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("purse") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.user")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return false;
            }
            if (args.length != 0) {
                player.sendMessage(Component.text("Usage: /purse", NamedTextColor.RED));
                return false;
            }
            inventoryManager.getInventory("PurseMenu").open(player);
            return true;
        }
        return false;
    }
}
