package com.github.yuqingliu.economy.commands;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor
public class VendorItemCommand implements CommandExecutor {
    @Inject
    private final VendorService vendorService;
    @Inject
    private final CurrencyService currencyService;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vendoritem") && sender instanceof Player) {
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
                    if(args.length < 6) {
                        return false;
                    }
                    Map<String, Double> buyPrices = new LinkedHashMap<>();
                    Map<String, Double> sellPrices = new LinkedHashMap<>();
                    try {
                        for (int i = 3; i < args.length; i+=3) {
                            if(currencyService.getCurrencyByName(args[i]) == null) {
                                return false;
                            }
                            buyPrices.put(args[i], Double.parseDouble(args[i+1]));
                            sellPrices.put(args[i], Double.parseDouble(args[i+2]));
                        }
                    } catch (Exception e) {
                        return false;
                    }
                    if(vendorService.addVendorItem(args[1], args[2], icon, buyPrices, sellPrices)) {
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
                    vendorService.deleteVendorItem(args[1], args[2], PlainTextComponentSerializer.plainText().serialize(item.displayName()));
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
