package com.github.yuqingliu.economy.view.shopmenu.buyordermenu;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.entities.ShopItemEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;
import com.github.yuqingliu.economy.view.shopmenu.ordermenu.OrderMenuController;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class BuyOrderMenuController extends AbstractPlayerInventoryController<ShopMenu> {
    private final int[] prevMenuButton = new int[]{0,0};
    private final int[] exitMenuButton = new int[]{1,0};
    private final int[] itemSlot = new int[]{4,1};
    private final int[] changeQuantityButton = new int[]{1,4};
    private final int[] changePriceButton = new int[]{3,4};
    private final int[] changeCurrencyTypeButton = new int[]{5,4};
    private final int[] confirmOrderButton = new int[]{7,4};
    private final Component changeComponent = Component.text("Click to Change", NamedTextColor.RED);
    @Setter private String currencyType;
    @Setter private int quantity;
    @Setter private double price;
    private ShopItemEntity item;
    
    public BuyOrderMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
    }

    public void openMenu(ShopItemEntity item) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.BuyOrderMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayItem();
        displayCurrencyType();
        displayQuantity();
        displayPrice();
        displayConfirmButton();
    }

    public void changeCurrencyType() {
        inventory.close();
        PlayerInventory shop = menu.getPluginManager().getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));
        Consumer<String> callback = (userInput) -> {
            this.inventory = shop.load(player);
            BuyOrderMenuController controller = menu.getBuyOrderMenu().getControllers().getPlayerInventoryController(player, this);
            try {
                if(menu.getCurrencyService().getCurrencyByName(userInput) == null) {
                    throw new IllegalArgumentException();
                }
                controller.setCurrencyType(userInput);
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid currency.");
            }
            controller.openMenu(item);
        };        
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("currency", NamedTextColor.RED));
        scanner.open(player);
    }

    public void changeQuantity() {
        inventory.close();
        PlayerInventory shop = menu.getPluginManager().getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));
        Consumer<String> callback = (userInput) -> {
            this.inventory = shop.load(player);
            BuyOrderMenuController controller = menu.getBuyOrderMenu().getControllers().getPlayerInventoryController(player, this);
            try {
                int quantity = Integer.parseInt(userInput);
                if(quantity < 1) {
                    throw new IllegalArgumentException();
                }
                controller.setQuantity(quantity);
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid quantity.");
            }
            controller.openMenu(item);
        };        
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("quantity", NamedTextColor.RED));
        scanner.open(player);
    }

    public void changeUnitPrice() {
        inventory.close();
        PlayerInventory shop = menu.getPluginManager().getInventoryManager().getInventory(ShopMenu.class.getSimpleName());
        shop.setDisplayName(Component.text(item.getShopName(), NamedTextColor.DARK_GRAY));
        Consumer<String> callback = (userInput) -> {
            this.inventory = shop.load(player);
            BuyOrderMenuController controller = menu.getBuyOrderMenu().getControllers().getPlayerInventoryController(player, this);
            try {
                double unitPrice = Double.parseDouble(userInput);
                if(unitPrice < 0) {
                    throw new IllegalArgumentException();
                }
                controller.setPrice(unitPrice);
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid unit price.");
            }
            controller.openMenu(item);
        };        
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("unit price", NamedTextColor.RED));
        scanner.open(player);
    }

    public void confirmOrder() {
        if(menu.getShopService().createBuyOrder(player, item, quantity, price, currencyType)) {
            menu.getOrderMenu().getControllers().getPlayerInventoryController(player, new OrderMenuController(player, inventory, menu)).openMenu(item);
        }
    }

    private void displayCurrencyType() {
        if(currencyType == null) {
            setItem(changeCurrencyTypeButton, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Change Bid Currency", NamedTextColor.DARK_AQUA)));
        } else {
            Component prefix = Component.text("Bid Currency: ", NamedTextColor.DARK_GRAY);
            Component currency = Component.text(currencyType, NamedTextColor.GOLD);
            Component currencyComponent = prefix.append(currency);
            setItem(changeCurrencyTypeButton, createSlotItem(Material.OAK_HANGING_SIGN, currencyComponent, changeComponent));
        }
    }

    private void displayQuantity() {
        Component prefix = Component.text("Quantity: ", NamedTextColor.DARK_GRAY);
        Component qty = Component.text(String.format("%d", quantity), NamedTextColor.GREEN);
        Component qtyComponent = prefix.append(qty);
        setItem(changeQuantityButton, createSlotItem(Material.OAK_HANGING_SIGN, qtyComponent, changeComponent));
    }

    private void displayPrice() {
        Component prefix = Component.text("Unit Price: ", NamedTextColor.DARK_GRAY);
        Component uprice = Component.text(String.format("%.2f", price), NamedTextColor.DARK_GREEN);
        Component upriceComponent = prefix.append(uprice);
        setItem(changePriceButton, createSlotItem(Material.OAK_HANGING_SIGN, upriceComponent, changeComponent));
    }

    private void displayConfirmButton() {
        Component currency = Component.text(currencyType == null ? "[Invalid currency]" : currencyType, NamedTextColor.GOLD);
        Component prefixQty = Component.text("Quantity: ", NamedTextColor.DARK_GRAY);
        Component qty = Component.text(String.format("%d", quantity), NamedTextColor.GREEN);
        Component qtyComponent = prefixQty.append(qty);
        Component prefixPrice = Component.text("Unit Price: ", NamedTextColor.DARK_GRAY);
        Component uprice = Component.text(String.format("%.2f ", price), NamedTextColor.DARK_GREEN);
        Component upriceComponent = prefixPrice.append(uprice).append(currency);
        Component profitPrefix = Component.text("Create Cost: ", NamedTextColor.DARK_GRAY);
        Component cost = Component.text(String.format("%.2f ", price * quantity), NamedTextColor.DARK_PURPLE);
        Component costComponent = profitPrefix.append(cost).append(currency);
        List<Component> lore = Arrays.asList(
            qtyComponent,
            upriceComponent,
            costComponent
        );
        setItem(confirmOrderButton, createSlotItem(Material.NETHERITE_BLOCK, Component.text("Confirm Order", NamedTextColor.GREEN), lore));
    }

    private void displayItem() {
        setItem(itemSlot, item.getIcon().clone());
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{3,0}, 3, 3, borderItem);
    }

    private void buttons() {
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
    }
}
