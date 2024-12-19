package com.github.yuqingliu.economy.view.shopmenu.buyorderdetails;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.buyordersmenu.BuyOrdersMenuController;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class BuyOrderDetailsMenuController extends AbstractPlayerInventoryController<ShopMenu> {
    private final int[] itemSlot = new int[]{4,1};
    private final int[] orderInfo = new int[]{4,3};
    private final int[] prevMenuButton = new int[]{2,1};
    private final int[] exitMenuButton = new int[]{6,1};
    private final int[] cancelOrderButton = new int[]{2,3};
    private final int[] claimOrderButton = new int[]{6,3};
    private final int[] refreshButton = new int[]{4,4};
    private ShopOrderEntity order;
    
    public BuyOrderDetailsMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
    }   

    public void openMenu(ShopOrderEntity order) {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.BuyOrderDetailsMenu);
        }, Duration.ofMillis(50));
        reload();
    }

    public void reload() {
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        displayItem();
        displayOrderInfo();
        buttons();
    }

    public void cancelOrder() {
        Scheduler.runAsync((task) -> {
            if(menu.getShopService().cancelBuyOrder(order, player)) {
                menu.getBuyOrdersMenu().getControllers().getPlayerInventoryController(player, new BuyOrdersMenuController(player, inventory, menu));
                return;
            }
            reload();
        });
    }

    public void claimOrder() {
        Scheduler.runAsync((task) -> {
            if(menu.getShopService().claimBuyOrder(order, player)) {
                menu.getBuyOrdersMenu().getControllers().getPlayerInventoryController(player, new BuyOrdersMenuController(player, inventory, menu));
                return;
            } 
            reload();
        });
    }

    private void displayItem() {
        ItemStack item = order.getShopItem().getIcon().clone();
        setItem(itemSlot, item);
    }

    private void displayOrderInfo() {
        ItemStack orderIcon = new ItemStack(Material.CREEPER_BANNER_PATTERN);
        ItemMeta meta = orderIcon.getItemMeta();
        if(meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.displayName(Component.text("BUY ORDER", NamedTextColor.GOLD));
            Component nameComponent = Component.text(order.getItemName(), NamedTextColor.AQUA);
            Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD));
            Component priceComponent = Component.text("Unit Buy Price: ", NamedTextColor.BLUE).append(Component.text(order.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
            Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() + "x", NamedTextColor.GREEN));
            Component quantityBoughtComponent = Component.text("Quantity Bought: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x", NamedTextColor.GREEN));
            meta.lore(Arrays.asList(nameComponent, currencyComponent, priceComponent, quantityComponent, quantityBoughtComponent));
        }
        orderIcon.setItemMeta(meta);
        setItem(orderInfo, orderIcon);
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{1,2}, 3, 7, borderItem);
        fillRectangleArea(new int[]{3,0}, 2, 3, borderItem);
    }

    private void buttons() {
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(refreshButton, getReloadIcon());
        double refund = (order.getQuantity() - order.getFilledQuantity()) * order.getUnitPrice(); 
        List<Component> cancelLore = Arrays.asList(
            Component.text("Refund: ", NamedTextColor.BLUE).append(Component.text(refund + "$ ", NamedTextColor.DARK_GREEN).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD))),
            Component.text("Return: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x ", NamedTextColor.DARK_GREEN).append(Component.text("items", NamedTextColor.GOLD)))
        );
        ItemStack cancelButton = createSlotItem(Material.RED_CONCRETE, Component.text("Cancel Order", NamedTextColor.RED), cancelLore);
        setItem(cancelOrderButton, cancelButton);
        ItemStack confirmButton = createSlotItem(Material.LIME_CONCRETE, Component.text("Claim Order"), Component.text("Collect: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x ", NamedTextColor.DARK_GREEN).append(Component.text("items", NamedTextColor.GOLD))));
        setItem(claimOrderButton, confirmButton);
    }
}
