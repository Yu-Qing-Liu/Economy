package com.github.yuqingliu.economy.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.services.ShopService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor
public class ShopItemCommand implements CommandExecutor {
    @Inject
    private final ShopService shopService;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("shopitem") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("economy.admin")) {
                player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return false;
            }
            if(args.length < 3) {
                return false;
            }
            switch (args[0]) {
                case "add":
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        player.sendMessage(Component.text("Invalid item icon. Please have a valid item in main hand.", NamedTextColor.RED));
                        return false;
                    }
                    icon.setAmount(1);
                    if(shopService.addShopItem(args[1], args[2], icon)) {
                        player.sendMessage(Component.text("Successfully added item to shop", NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("Invalid parameters. Please enter valid fields.", NamedTextColor.RED));
                        return false;
                    }
                    break;
                case "remove":
                    ItemStack item = player.getInventory().getItemInMainHand().clone();
                    if(item.getType() == Material.AIR) {
                        player.sendMessage(Component.text("Invalid item icon. Please have a valid item in main hand.", NamedTextColor.RED));
                        return false;
                    }
                    shopService.deleteShopItem(args[1], args[2], PlainTextComponentSerializer.plainText().serialize(item.displayName()));
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
