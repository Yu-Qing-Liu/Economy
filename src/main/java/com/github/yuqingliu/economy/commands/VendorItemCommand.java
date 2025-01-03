package com.github.yuqingliu.economy.commands;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.services.CurrencyService;
import com.github.yuqingliu.economy.persistence.services.VendorService;
import com.google.inject.Inject;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VendorItemCommand implements CommandExecutor {
    private final VendorService vendorService;
    private final CurrencyService currencyService;
    private final Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vendoritem") && sender instanceof Player) {
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
                    if(args.length < 6) {
                        return false;
                    }
                    ItemStack icon = player.getInventory().getItemInMainHand().clone();
                    if(icon.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid item icon. Please have a valid item in main hand.");
                        return false;
                    }
                    icon.setAmount(1);
                    Map<String, Double> buyPrices = new LinkedHashMap<>();
                    Map<String, Double> sellPrices = new LinkedHashMap<>();
                    try {
                        for (int i = 3; i < args.length; i+=3) {
                            if(currencyService.getCurrencyByName(args[i]) == null) {
                                logger.sendPlayerErrorMessage(player, "Invalid currency parameters. Please enter valid parameters.");
                                return false;
                            }
                            buyPrices.put(args[i], Double.parseDouble(args[i+1]));
                            sellPrices.put(args[i], Double.parseDouble(args[i+2]));
                        }
                    } catch (Exception e) {
                        return false;
                    }
                    if(vendorService.addVendorItem(args[1], args[2], icon, buyPrices, sellPrices)) {
                        logger.sendPlayerAcknowledgementMessage(player, "Successfully added item to vendor");
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Could not add item to vendor.");
                        return false;
                    }
                case "update":
                    if(args.length < 6) {
                        return false;
                    }
                    ItemStack option = player.getInventory().getItemInMainHand().clone();
                    if(option.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid item icon. Please have a valid item in main hand.");
                        return false;
                    }
                    option.setAmount(1);
                    Map<String, Double> newBuyPrices = new LinkedHashMap<>();
                    Map<String, Double> newSellPrices = new LinkedHashMap<>();
                    try {
                        for (int i = 3; i < args.length; i+=3) {
                            if(currencyService.getCurrencyByName(args[i]) == null) {
                                logger.sendPlayerErrorMessage(player, "Invalid currency parameters. Please enter valid parameters.");
                                return false;
                            }
                            newBuyPrices.put(args[i], Double.parseDouble(args[i+1]));
                            newSellPrices.put(args[i], Double.parseDouble(args[i+2]));
                        }
                    } catch (Exception e) {
                        return false;
                    }
                    if(vendorService.updateVendorItem(args[1], args[2], option, newBuyPrices, newSellPrices)) {
                        logger.sendPlayerAcknowledgementMessage(player, "Successfully updated item");
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Could not update item.");
                        return false;
                    }

                case "remove":
                    ItemStack item = player.getInventory().getItemInMainHand().clone();
                    if(item.getType() == Material.AIR) {
                        logger.sendPlayerErrorMessage(player, "Invalid item icon. Please have a valid item in main hand.");
                        return false;
                    }
                    if(vendorService.deleteVendorItem(args[1], args[2], PlainTextComponentSerializer.plainText().serialize(item.displayName()))) {
                        logger.sendPlayerAcknowledgementMessage(player, "Successfully deleted item from vendor.");
                        return true;
                    } else {
                        logger.sendPlayerErrorMessage(player, "Could not delete item from vendor.");
                        return false;
                    }
                default:
                    return false;
            }
        }
        return false;
    }
}
