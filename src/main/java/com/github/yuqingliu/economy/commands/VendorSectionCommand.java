package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@RequiredArgsConstructor
public class VendorSectionCommand implements CommandExecutor {
    @Inject
    private final VendorService vendorService;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vendorsection") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return false;
            }
            if (args.length != 3) {
                return false;
            }
            switch (args[0]) {
                case "create":
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        player.sendMessage(Component.text("Invalid section icon. Please have a valid item in main hand.", NamedTextColor.RED));
                        return false;
                    }
                    icon.setAmount(1);
                    ItemMeta meta = icon.getItemMeta();
                    if(meta != null) {
                        meta.displayName(Component.text(args[2], NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
                        icon.setItemMeta(meta);
                    }
                    if(vendorService.addVendorSection(args[1], args[2], icon)) {
                        player.sendMessage(Component.text("Successfully added section with name " + args[2] + " in vendor " + args[1], NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("Invalid parameters. Please enter valid fields.", NamedTextColor.RED));
                        return false;
                    }
                    break;
                case "delete":
                    vendorService.deleteVendorSection(args[1], args[2]);
                    player.sendMessage(Component.text("Section with name " + args[2] + " has been deleted from vendor " + args[1], NamedTextColor.RED));
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }
}
