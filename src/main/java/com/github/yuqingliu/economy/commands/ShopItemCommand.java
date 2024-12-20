package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ShopItemCommand implements CommandExecutor {
    private final ShopService shopService;
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("shopitem") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                logger.sendPlayerErrorMessage(player, "You do not have permission to use this command.");
                return false;
            }
            if(args.length < 3) {
                return false;
            }
            switch (args[0]) {
                case "add":
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid item icon. Please have a valid item in main hand.");
                        return false;
                    }
                    icon.setAmount(1);
                    if(shopService.addShopItem(args[1], args[2], icon)) {
                        logger.sendPlayerAcknowledgementMessage(player, "Successfully added item to shop");
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Invalid parameters. Please enter valid parameters.");
                        return false;
                    }
                case "remove":
                    ItemStack item = player.getInventory().getItemInMainHand().clone();
                    if(item.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid item icon. Please have a valid item in main hand.");
                        return false;
                    }
                    if(shopService.deleteShopItem(args[1], args[2], PlainTextComponentSerializer.plainText().serialize(item.displayName()))) {
                        logger.sendPlayerAcknowledgementMessage(player, String.format("Item sucessfully deleted from shop %s", args[1]));
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Shop item could not be deleted.");
                        return false;
                    }
                default:
                    return false;
            }
        }
        return false;
    }
}
