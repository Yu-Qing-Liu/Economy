package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@RequiredArgsConstructor
public class VendorItemCommand implements CommandExecutor {
    @Inject
    private final VendorService vendorService;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vendoritem") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return false;
            }
            if (args.length > 6 || args.length < 4) {
                return false;
            }
            switch (args[0]) {
                case "add":
                    if(args.length != 6) {
                        return false;
                    }
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        player.sendMessage(Component.text("Invalid section icon. Please have a valid item in main hand.", NamedTextColor.RED));
                        return false;
                    }
                    icon.setAmount(1);
                    double buyPrice = Double.parseDouble(args[4]);
                    double sellPrice = Double.parseDouble(args[5]);
                    if(vendorService.addVendorItem(args[1], args[2], icon, args[3], buyPrice, sellPrice)) {
                        player.sendMessage(Component.text("Successfully added item to shop", NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("Invalid parameters. Please enter valid fields.", NamedTextColor.RED));
                        return false;
                    }
                    break;
                case "remove":
                    if(args.length != 4) {
                        return false;
                    }
                    vendorService.deleteVendorItem(args[1], args[2], args[3]);
                    player.sendMessage(Component.text("Item sucessfully deleted from shop" + args[1], NamedTextColor.RED));
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }
}
