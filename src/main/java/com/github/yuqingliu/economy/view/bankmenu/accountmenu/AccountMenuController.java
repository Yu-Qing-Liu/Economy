package com.github.yuqingliu.economy.view.bankmenu.accountmenu;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu;
import com.github.yuqingliu.economy.view.bankmenu.BankMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class AccountMenuController {
    private final BankMenu bankMenu;
    private final int prev = 10;
    private final int exit = 11;
    private final int accountDetail = 12;
    private final int deposit = 14;
    private final int withdraw = 16;
    private Map<Player, AccountEntity> accounts = new ConcurrentHashMap<>();

    public AccountMenuController(BankMenu bankMenu) {
        this.bankMenu = bankMenu;
    }
    
    public void openAccountMenu(Player player, Inventory inv, AccountEntity account) {
        this.accounts.put(player, account);
        Scheduler.runLaterAsync((task) -> {
            bankMenu.getPlayerMenuTypes().put(player, MenuType.AccountMenu);
        }, Duration.ofMillis(50));
        bankMenu.clear(inv);
        frame(inv);
        accountDetails(inv, player);
        buttons(inv);
    }

    public void onClose(Player player) {
        accounts.remove(player);
    }

    private void accountDetails(Inventory inv, Player player) {
        AccountEntity account = accounts.get(player);
        ItemStack icon = account.getIcon().clone();
        ItemMeta meta = icon.getItemMeta();
        if(meta != null) {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Interest Rate: ", NamedTextColor.DARK_AQUA).append(Component.text(account.getInterestRate() + "%", NamedTextColor.DARK_GREEN)));
            for(CurrencyEntity currency : account.getCurrencies()) {
                Component currencyInfo = Component.text(String.format("%s: ", currency.getCurrencyName()), NamedTextColor.BLUE).append(Component.text(currency.getAmount(), NamedTextColor.GOLD));
                lore.add(currencyInfo);
            }
            meta.lore(lore);
        }
        icon.setItemMeta(meta);
        inv.setItem(accountDetail, icon);
    }

    private void frame(Inventory inv) {
        ItemStack Placeholder = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = Placeholder.getItemMeta();
        if(meta != null) {
            meta.displayName(Component.text("Unavailable", NamedTextColor.DARK_PURPLE));
        }
        Placeholder.setItemMeta(meta);
        for (int i = 0; i < bankMenu.getInventorySize(); i++) {
            inv.setItem(i, Placeholder);
        }
    }

    private void buttons(Inventory inv) {
        ItemStack prev = new ItemStack(Material.GREEN_WOOL);
        ItemMeta pmeta = prev.getItemMeta();
        if(pmeta != null) {
            pmeta.displayName(Component.text("Previous", NamedTextColor.GREEN));
        }
        prev.setItemMeta(pmeta);
        inv.setItem(this.prev, prev);

        ItemStack exit = new ItemStack(Material.RED_WOOL);
        ItemMeta emeta = exit.getItemMeta();
        if(emeta != null) {
            emeta.displayName(Component.text("Exit", NamedTextColor.RED));
        }
        exit.setItemMeta(emeta);
        inv.setItem(this.exit, exit);

        ItemStack deposit = new ItemStack(Material.ENDER_CHEST);
        ItemMeta dmeta = deposit.getItemMeta();
        if(dmeta != null) {
            dmeta.displayName(Component.text("Deposit", NamedTextColor.GOLD));
        }
        deposit.setItemMeta(dmeta);
        inv.setItem(this.deposit, deposit);

        ItemStack withdraw = new ItemStack(Material.CHEST);
        ItemMeta wmeta = withdraw.getItemMeta();
        if(wmeta != null) {
            wmeta.displayName(Component.text("Withdraw", NamedTextColor.RED));
        }
        withdraw.setItemMeta(wmeta);
        inv.setItem(this.withdraw, withdraw);
    }
}
