package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.logger.Logger;
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
    @Inject
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vendorsection") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            if (args.length != 3) {
                return false;
            }
            switch (args[0]) {
                case "create":
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid item icon. Please have a valid item in main hand.");
                        return false;
                    }
                    icon.setAmount(1);
                    ItemMeta meta = icon.getItemMeta();
                    if(meta != null) {
                        meta.displayName(Component.text(args[2], NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
                        icon.setItemMeta(meta);
                    }
                    if(vendorService.addVendorSection(args[1], args[2], icon)) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Successfully added section with name %s in vendor %s", args[2], args[1]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Could not add section");
                        return false;
                    }
                case "delete":
                    if(vendorService.deleteVendorSection(args[1], args[2])) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Section with name %s has been deleted from vendor %s", args[2], args[1]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Section could not be deleted.");
                        return false;
                    }
                default:
                    return false;
            }
        }
        return false;
    }
}
