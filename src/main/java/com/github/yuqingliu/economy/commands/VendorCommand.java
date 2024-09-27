package com.github.yuqingliu.economy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.github.yuqingliu.economy.api.managers.NameSpacedKeyManager;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@RequiredArgsConstructor
public class VendorCommand implements CommandExecutor {
    @Inject
    private final NameSpacedKeyManager nameSpacedKeyManager;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vendor") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return false;
            }
            if (args.length != 2) {
                player.sendMessage(Component.text("Usage: /vendor create <name>; /vendor delete <name>", NamedTextColor.RED));
                return false;
            }
            switch (args[0]) {
                case "create":
                    //new AddShop(args[1]).update();
                    player.sendMessage(Component.text("Successfully created shop with name " + args[1], NamedTextColor.GREEN));
                    Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                    PersistentDataContainer container = villager.getPersistentDataContainer();
                    container.set(nameSpacedKeyManager.getVendorKey(), PersistentDataType.STRING, args[1]);
                    villager.setPersistent(true);
                    break;
                case "delete":
                    //new RemoveShop(args[1]).update();
                    player.sendMessage(Component.text("Vendor with name " + args[1] + " has been deleted", NamedTextColor.RED));
                    break;
                default:
                    player.sendMessage(Component.text("Usage: /vendor create <name>; /vendor delete <name>", NamedTextColor.RED));
                    return false;
            }
            return true;
        }
        return false;
    }
}
