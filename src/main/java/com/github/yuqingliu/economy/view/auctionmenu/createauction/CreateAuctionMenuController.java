package com.github.yuqingliu.economy.view.auctionmenu.createauction;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu.MenuType;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class CreateAuctionMenuController extends AbstractPlayerInventoryController<AuctionMenu> {
    private final int[] itemSlot = new int[] { 4, 1 };
    private final int[] placeItemIndicator = new int[] { 4, 2 };
    private final int[] changeAuctionDelay = new int[] { 2, 4 };
    private final int[] changeAuctionDuration = new int[] { 3, 4 };
    private final int[] changeBidCurrency = new int[] { 4, 4 };
    private final int[] changeStartingBid = new int[] { 5, 4 };
    private final int[] confirmButton = new int[] { 6, 4 };
    private final int[] previousMenuButton = new int[] { 0, 0 };
    private final int[] exitMenuButton = new int[] { 1, 0 };
    private final Component changeComponent = Component.text("Click to Change", NamedTextColor.DARK_RED);
    @Setter
    private double startingBid = 0;
    @Setter
    private String bidCurrency;
    @Setter
    private Duration auctionDelay = Duration.ZERO;
    @Setter
    private Duration auctionDuration = Duration.ofMinutes(1); 

    public CreateAuctionMenuController(Player player, Inventory inventory, AuctionMenu auctionMenu) {
        super(player, inventory, auctionMenu);
    }

    public void openMenu() {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.CreateAuctionMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.ORANGE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayAuctionDelay();
        displayAuctionDuration();
        displayBidCurrency();
        displayStartingBid();
        displayConfirmButton();
    }

    public void onClose() {
        ItemStack slotItem = getItem(itemSlot);
        if (slotItem != null && slotItem.getType() != Material.AIR) {
            menu.getPluginManager().getInventoryManager().addItemToPlayer(player, slotItem, slotItem.getAmount());
            setItem(itemSlot, new ItemStack(Material.AIR));
        }
    }

    public void changeStartingBid() {
        inventory.close();
        PlayerInventory auctionHouse = menu.getPluginManager().getInventoryManager()
                .getInventory(AuctionMenu.class.getSimpleName());
        Consumer<String> callback = (userInput) -> {
            this.inventory = auctionHouse.load(player);
            CreateAuctionMenuController controller = menu.getCreateAuctionMenu().getControllers().getPlayerInventoryController(player, this);
            try {
                double bid = Double.parseDouble(userInput);
                if (bid < 0) {
                    throw new IllegalArgumentException();
                }
                controller.setStartingBid(bid);
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid amount.");
            }
            controller.openMenu();
        };
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("bid amount", NamedTextColor.RED));
        scanner.open(player);
    }

    public void changeBidCurrency() {
        inventory.close();
        PlayerInventory auctionHouse = menu.getPluginManager().getInventoryManager().getInventory(AuctionMenu.class.getSimpleName());
        Consumer<String> callback = (userInput) -> {
            this.inventory = auctionHouse.load(player);
            CreateAuctionMenuController controller = menu.getCreateAuctionMenu().getControllers().getPlayerInventoryController(player, this);
            try {
                if(menu.getCurrencyService().getCurrencyByName(userInput) == null) {
                    throw new IllegalArgumentException();
                }
                controller.setBidCurrency(userInput);
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid currency.");
            }
            controller.openMenu();
        };
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("bid currency", NamedTextColor.RED));
        scanner.open(player);
    }

    public void changeAuctionDelay() {
        inventory.close();
        PlayerInventory auctionHouse = menu.getPluginManager().getInventoryManager().getInventory(AuctionMenu.class.getSimpleName());
        Consumer<String> callback = (userInput) -> {
            this.inventory = auctionHouse.load(player);
            menu.getCreateAuctionMenu().getControllers().getPlayerInventoryController(player, this);
            try {
                long time = Long.parseLong(userInput);
                if(time < 0) {
                    throw new IllegalArgumentException();
                }
                this.setAuctionDelay(Duration.ofMinutes(time));
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid delay.");
            }
            this.openMenu();
        };
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("delay (mins)", NamedTextColor.RED));
        scanner.open(player);
    }

    public void changeAuctionDuration() {
        inventory.close();
        PlayerInventory auctionHouse = menu.getPluginManager().getInventoryManager().getInventory(AuctionMenu.class.getSimpleName());
        Consumer<String> callback = (userInput) -> {
            this.inventory = auctionHouse.load(player);
            menu.getCreateAuctionMenu().getControllers().getPlayerInventoryController(player, this);
            try {
                long time = Long.parseLong(userInput);
                if(time < 1) {
                    throw new IllegalArgumentException();
                }
                this.setAuctionDuration(Duration.ofMinutes(time));
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid duration.");
            }
            this.openMenu();
        };
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager().getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("duration (mins)", NamedTextColor.RED));
        scanner.open(player);
    }

    public void confirm() {
        Instant start = Instant.now().plus(auctionDelay);
        ItemStack item = getItem(itemSlot);
        if(item == null || item.getType() == Material.AIR) {
            menu.getLogger().sendPlayerErrorMessage(player, "No item in item slot.");
            return;
        }
        if(menu.getCurrencyService().getCurrencyByName(bidCurrency) == null) {
            menu.getLogger().sendPlayerErrorMessage(player, "Invalid currency.");
            return;
        }
        if(menu.getAuctionService().startAuction(player, getItem(itemSlot), startingBid, bidCurrency, start, auctionDuration)) {
            setItem(itemSlot, new ItemStack(Material.AIR));
            menu.getLogger().sendPlayerNotificationMessage(player, "Auction created");
        }
    }

    private void displayAuctionDelay() {
        String duration = durationToString(auctionDelay);
        Component prefix = Component.text("Starts After: ", NamedTextColor.DARK_GRAY);
        Component time = Component.text(duration, NamedTextColor.GREEN);
        Component durationComponent = prefix.append(time);
        setItem(changeAuctionDelay, createSlotItem(Material.OAK_HANGING_SIGN, durationComponent, changeComponent));
    }

    private void displayAuctionDuration() {
        String duration = durationToString(auctionDuration);
        Component prefix = Component.text("Auction Duration: ", NamedTextColor.DARK_GRAY);
        Component time = Component.text(duration, NamedTextColor.GREEN);
        Component durationComponent = prefix.append(time);
        setItem(changeAuctionDuration, createSlotItem(Material.OAK_HANGING_SIGN, durationComponent, changeComponent));
    }

    private void displayBidCurrency() {
        if(bidCurrency == null) {
            setItem(changeBidCurrency, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Change Bid Currency", NamedTextColor.DARK_AQUA)));
        } else {
            Component prefix = Component.text("Bid Currency: ", NamedTextColor.DARK_GRAY);
            Component currency = Component.text(bidCurrency, NamedTextColor.GOLD);
            Component currencyComponent = prefix.append(currency);
            setItem(changeBidCurrency, createSlotItem(Material.OAK_HANGING_SIGN, currencyComponent, changeComponent));
        }
    }

    private void displayStartingBid() {
        Component prefix = Component.text("Starting Bid: ", NamedTextColor.DARK_GRAY);
        Component bid = Component.text(String.format("%.2f", startingBid), NamedTextColor.GREEN);
        Component bidComponent = prefix.append(bid);
        setItem(changeStartingBid, createSlotItem(Material.OAK_HANGING_SIGN, bidComponent, changeComponent));
    }

    private void displayConfirmButton() {
        String delay = durationToString(auctionDelay);
        Component prefixDelay = Component.text("Starts After: ", NamedTextColor.DARK_GRAY);
        Component timeDelay = Component.text(delay, NamedTextColor.GREEN);
        Component delayComponent = prefixDelay.append(timeDelay);
        String duration = durationToString(auctionDuration);
        Component prefix = Component.text("Auction Duration: ", NamedTextColor.DARK_GRAY);
        Component time = Component.text(duration, NamedTextColor.GREEN);
        Component durationComponent = prefix.append(time);
        Component bidPrefix = Component.text("Starting Bid: ");
        Component bid = Component.text(String.format("%.2f ", startingBid), NamedTextColor.GREEN);
        String curr = bidCurrency == null ? "[Invalid Currency]" : bidCurrency;
        Component currency = Component.text(curr, NamedTextColor.GOLD);
        Component bidComponent = bidPrefix.append(bid).append(currency);
        List<Component> lore = Arrays.asList(
            delayComponent,
            durationComponent,
            bidComponent
        );
        setItem(confirmButton, createSlotItem(Material.NETHERITE_BLOCK, Component.text("Confirm Auction", NamedTextColor.GREEN), lore));   
    }

    private void border() {
        fillRectangleArea(new int[] { 3, 0 }, 3, 3, getBackgroundTile(Material.BLACK_STAINED_GLASS_PANE));
    }

    private void buttons() {
        setItem(previousMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(itemSlot, new ItemStack(Material.AIR));
        setItem(placeItemIndicator, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Place Item Here", NamedTextColor.GREEN)));
    }
}
