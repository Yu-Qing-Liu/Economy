package com.github.yuqingliu.economy.view.auctionmenu.bidmenu;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.api.view.PlayerInventory;
import com.github.yuqingliu.economy.persistence.entities.AuctionEntity;
import com.github.yuqingliu.economy.persistence.entities.BidEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu.MenuType;
import com.github.yuqingliu.economy.view.auctionmenu.playerauctions.PlayerAuctionsMenuController;
import com.github.yuqingliu.economy.view.textmenu.TextMenu;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class BidMenuController extends AbstractPlayerInventoryController<AuctionMenu> {
    private AuctionEntity auction;
    private final int[] itemSlot = new int[] { 4, 1 };
    private final int[] changeBidAmount = new int[] { 6, 3 };
    private final int[] confirmBid = new int[] { 6, 4 };
    private final int[] previousMenuButton = new int[] { 2, 3 };
    private final int[] exitMenuButton = new int[] { 2, 4 };
    private final int[] refreshButton = new int[] { 4, 3 };
    private final int[] collectButton = new int[] { 4, 4 };
    @Setter
    private double bid = 0;

    public BidMenuController(Player player, Inventory inventory, AuctionMenu auctionMenu) {
        super(player, inventory, auctionMenu);
    }

    public void openMenu(AuctionEntity auction) {
        this.auction = auction;
        if (bid == 0) {
            this.bid = auction.getHighestBid() + 0.01;
        }
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.BidMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.PURPLE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayCollectButton();
        displayConfirmBidButton();
        Scheduler.runAsync((task) -> {
            reload();
        });
    }

    public void reload() {
        fetchAuction();
        displayAuction();
    }

    public void changeBidAmount() {
        if (auction.getEnd().isBefore(Instant.now())) {
            menu.getLogger().sendPlayerErrorMessage(player, "This auction has ended already.");
            return;
        }
        inventory.close();
        PlayerInventory auctionHouse = menu.getPluginManager().getInventoryManager()
                .getInventory(AuctionMenu.class.getSimpleName());
        Consumer<String> callback = (userInput) -> {
            Inventory inventory = auctionHouse.load(player);
            BidMenuController controller = menu.getBidMenu().getControllers().getPlayerInventoryController(player,
                    new BidMenuController(player, inventory, menu));
            try {
                if (!(Double.parseDouble(userInput) > auction.getHighestBid())) {
                    throw new IllegalArgumentException();
                }
                controller.setBid(Double.parseDouble(userInput));
            } catch (Exception e) {
                menu.getLogger().sendPlayerErrorMessage(player, "Invalid amount.");
                controller.setBid(auction.getHighestBid() + 0.01);
            }
            controller.openMenu(auction);
        };
        TextMenu scanner = (TextMenu) menu.getPluginManager().getInventoryManager()
                .getInventory(TextMenu.class.getSimpleName());
        scanner.setOnCloseCallback(callback);
        scanner.setDisplayName(Component.text("bid amount", NamedTextColor.RED));
        scanner.open(player);
    }

    public void collectAuction() {
        if (auction.getEnd().isAfter(Instant.now())) {
            menu.getLogger().sendPlayerErrorMessage(player, "Auction has not ended yet.");
            return;
        } else if (auction.getPlayerId().equals(player.getUniqueId())) {
            double refund = auction.getHighestBid();
            if (menu.getAuctionService().collectAuction(player, auction)) {
                menu.getLogger().sendPlayerNotificationMessage(player,
                        String.format("Collected auction with %.2f %s bid refund.", refund,
                                auction.getCurrencyType()));
            }
        } else if (auction.getBidderId().equals(player.getUniqueId())) {
            BidEntity playerBid = menu.getAuctionService().getPlayerBid(auction, player);
            double refund = playerBid.getAmount();
            if (menu.getAuctionService().collectAuctionItem(player, auction)) {
                menu.getLogger().sendPlayerNotificationMessage(player,
                        String.format("Collected auction with %.2f %s bid refund.", refund, auction.getCurrencyType()));
            }
        } else {
            BidEntity playerBid = menu.getAuctionService().getPlayerBid(auction, player);
            if (playerBid == null) {
                menu.getLogger().sendPlayerErrorMessage(player, "You did not bid on this item.");
                return;
            }
            double refund = playerBid.getAmount();
            if (menu.getAuctionService().collectBid(player, playerBid)) {
                menu.getLogger().sendPlayerNotificationMessage(player,
                        String.format("Collected %.2f %s bid refund.", refund, auction.getCurrencyType()));
            }
        }
        menu.getPlayerAuctionsMenu().getControllers()
                .getPlayerInventoryController(player, new PlayerAuctionsMenuController(player, inventory, menu))
                .openMenu();
    }

    public void confirmBid() {
        menu.getAuctionService().bid(auction, player, bid);
    }

    private void border() {
        fillRectangleArea(new int[] { 3, 0 }, 3, 3, getBackgroundTile(Material.BLACK_STAINED_GLASS_PANE));
    }

    private void fetchAuction() {
        this.auction = menu.getAuctionService().getAuction(this.auction.getAuctionId());
    }

    private void displayAuction() {
        ItemStack icon = auction.getItem().clone();
        ItemMeta meta = icon.getItemMeta();
        List<Component> lore = meta.lore() != null ? meta.lore() : new ArrayList<>();
        // Owner lore
        Component ownerLorePrefix = Component.text("Owner: ", NamedTextColor.DARK_GRAY);
        Component ownerLoreName = Component.text(Bukkit.getOfflinePlayer(auction.getPlayerId()).getName(),
                NamedTextColor.BLUE);
        Component ownerLore = ownerLorePrefix.append(ownerLoreName);
        lore.add(ownerLore);
        // Bidder lore
        if (auction.getBidderId() != null) {
            Component bidderLorePrefix = Component.text("Highest Bidder: ", NamedTextColor.DARK_GRAY);
            Component bidderLoreName = Component.text(Bukkit.getOfflinePlayer(auction.getBidderId()).getName(),
                    NamedTextColor.LIGHT_PURPLE);
            Component bidderLore = bidderLorePrefix.append(bidderLoreName);
            lore.add(bidderLore);
        }
        // Highest bid lore
        Component bidLorePrefix = Component.text("Highest Bid: ", NamedTextColor.DARK_GRAY);
        Component bidLoreAmount = Component.text(
                String.format("%.2f %s", auction.getHighestBid(), auction.getCurrencyType()), NamedTextColor.GOLD);
        Component bidLore = bidLorePrefix.append(bidLoreAmount);
        lore.add(bidLore);
        // Time lore
        Instant start = auction.getStart();
        Instant end = auction.getEnd();
        Instant now = Instant.now();
        if (now.isAfter(end)) {
            lore.add(Component.text(String.format("This auction has ended"), NamedTextColor.RED));
        } else if (now.isBefore(start)) {
            Duration duration = Duration.between(now, start);
            lore.add(Component.text(String.format("Starts in %s", durationToString(duration)), NamedTextColor.GREEN));
        } else {
            Duration duration = Duration.between(now, end);
            lore.add(Component.text(String.format("Ends in %s", durationToString(duration)), NamedTextColor.YELLOW));
        }
        meta.lore(lore);
        icon.setItemMeta(meta);
        setItem(itemSlot, icon);
    }

    private void displayConfirmBidButton() {
        if (auction.getEnd().isBefore(Instant.now())) {
            setItem(confirmBid, getBackgroundTile(Material.GRAY_WOOL));
        } else {
            CurrencyEntity currency = menu.getCurrencyService().getCurrencyByName(auction.getCurrencyType());
            setItem(confirmBid, createSlotItem(currency.getIcon().clone().getType(),
                    Component.text(String.format("Bid %.2f %s", bid, auction.getCurrencyType()), NamedTextColor.RED)));
        }
    }

    private void displayCollectButton() {
        if (auction.getEnd().isAfter(Instant.now())) {
            setItem(collectButton, createSlotItem(Material.GRAY_WOOL,
                    Component.text("Auction has not ended yet.", NamedTextColor.RED)));
        } else if (auction.getPlayerId().equals(player.getUniqueId())) {
            setItem(collectButton,
                    createSlotItem(Material.BLUE_WOOL, Component.text(
                            String.format("Collect %.2f %s", auction.getHighestBid(), auction.getCurrencyType()),
                            NamedTextColor.RED)));
        } else if (auction.getBidderId().equals(player.getUniqueId())) {
            setItem(collectButton,
                    createSlotItem(Material.BLUE_WOOL, Component.text("Collect Auction", NamedTextColor.RED)));
        }
    }

    private void buttons() {
        setItem(previousMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(refreshButton, getReloadIcon());
        if (auction.getEnd().isBefore(Instant.now())) {
            setItem(changeBidAmount, getBackgroundTile(Material.GRAY_WOOL));
        } else {
            setItem(changeBidAmount, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Change Bid Amount")));
        }
    }
}
