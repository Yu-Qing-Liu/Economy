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
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@RequiredArgsConstructor
public class VendorCommand implements CommandExecutor {
    @Inject
    private final NameSpacedKeyManager nameSpacedKeyManager;
    @Inject
    private final VendorService vendorService;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vendor") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return false;
            }
            if (args.length != 2) {
                return false;
            }
            switch (args[0]) {
                case "create":
                    vendorService.addVendor(args[1]);
                    player.sendMessage(Component.text("Successfully created shop with name " + args[1], NamedTextColor.GREEN));
                    Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
                    PersistentDataContainer container = villager.getPersistentDataContainer();
                    container.set(nameSpacedKeyManager.getVendorKey(), PersistentDataType.STRING, args[1]);
                    villager.setPersistent(true);
                    break;
                case "delete":
                    vendorService.deleteVendor(args[1]);
                    player.sendMessage(Component.text("Vendor with name " + args[1] + " has been deleted", NamedTextColor.RED));
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }
}
