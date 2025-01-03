package com.github.yuqingliu.economy.view.auctionmenu.mainmenu;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.AuctionEntity;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu;
import com.github.yuqingliu.economy.view.auctionmenu.AuctionMenu.MenuType;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.PageData;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class MainMenuController extends AbstractPlayerInventoryController<AuctionMenu> {
    private final int[] nextPageButton = new int[] { 8, 1 };
    private final int[] prevPageButton = new int[] { 8, 2 };
    private final int[] refreshButton = new int[] { 8, 3 };
    private final int[] exitMenuButton = new int[] { 8, 4 };
    private final int[] searchAuctionButton = new int[] { 0, 1 };
    private final int[] createAuctionButton = new int[] { 0, 2 };
    private final int[] playerAuctionsButton = new int[] { 0, 3 };
    private final int[] auctionsStart = new int[] { 1, 1 };
    private final int auctionsLength = 7;
    private final int auctionsWidth = 4;
    private final int auctionsSize = auctionsWidth * auctionsLength;
    private final List<int[]> auctions;
    private final PageData<AuctionEntity> pageData = new PageData<>();

    public MainMenuController(Player player, Inventory inventory, AuctionMenu auctionMenu) {
        super(player, inventory, auctionMenu);
        this.auctions = rectangleArea(auctionsStart, auctionsWidth, auctionsLength);
    }

    public void openMenu() {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.MainMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.YELLOW_STAINED_GLASS_PANE));
        buttons();
        rectangleAreaLoading(auctionsStart, auctionsWidth, auctionsLength);
        reload();
    }

    public void reload() {
        Scheduler.runAsync(t -> {
            fetchAuctions();
            displayAuctions();
        });
    }

    public void nextPage() {
        pageData.nextPage(() -> displayAuctions());
    }

    public void prevPage() {
        pageData.prevPage(() -> displayAuctions());
    }

    private void fetchAuctions() {
        pageData.clear();
        List<AuctionEntity> auctions = menu.getAuctionService().getActiveAuctions().stream()
                .filter(auc -> auc.getEnd().isAfter(Instant.now())).collect(Collectors.toList());
        if (auctions == null || auctions.isEmpty()) {
            return;
        }
        Queue<AuctionEntity> temp = new ArrayDeque<>();
        temp.addAll(auctions);
        int maxPages = (int) Math.ceil((double) auctions.size() / (double) auctionsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, AuctionEntity> options = new LinkedHashMap<>();
            for (int[] coords : this.auctions) {
                if (temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayAuctions() {
        Map<List<Integer>, AuctionEntity> options = pageData.getCurrentPageData();
        for (Map.Entry<List<Integer>, AuctionEntity> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            AuctionEntity auction = entry.getValue();
            if (auction == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack icon = auction.getItem().clone();
                ItemMeta meta = icon.getItemMeta();
                List<Component> lore = meta.lore() != null ? meta.lore() : new ArrayList<>();
                // Owner lore
                Component ownerLorePrefix = Component.text("Owner: ", NamedTextColor.DARK_GRAY);
                Component ownerLoreName = Component.text(Bukkit.getOfflinePlayer(auction.getPlayerId()).getName(), NamedTextColor.BLUE);
                Component ownerLore = ownerLorePrefix.append(ownerLoreName);
                lore.add(ownerLore);
                // Bidder lore
                if(auction.getBidderId() != null) {
                    Component bidderLorePrefix = Component.text("Highest Bidder: ", NamedTextColor.DARK_GRAY);
                    Component bidderLoreName = Component.text(Bukkit.getOfflinePlayer(auction.getBidderId()).getName(), NamedTextColor.LIGHT_PURPLE);
                    Component bidderLore = bidderLorePrefix.append(bidderLoreName);
                    lore.add(bidderLore);
                }
                // Highest bid lore
                Component bidLorePrefix = Component.text("Highest Bid: ", NamedTextColor.DARK_GRAY);
                Component bidLoreAmount = Component.text(String.format("%.2f %s", auction.getHighestBid(), auction.getCurrencyType()), NamedTextColor.GOLD);
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
                setItem(coords, icon);
            }
        }
    }

    private void buttons() {
        setItem(nextPageButton, getNextPageIcon());
        setItem(prevPageButton, getPrevPageIcon());
        setItem(refreshButton, getReloadIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(searchAuctionButton, createSlotItem(Material.OAK_HANGING_SIGN, Component.text("Search", NamedTextColor.BLUE)));
        setItem(createAuctionButton, createSlotItem(Material.CHEST, Component.text("Create Auction", NamedTextColor.AQUA)));
        setItem(playerAuctionsButton, createSlotItem(Material.ENDER_CHEST, Component.text("Your Auctions", NamedTextColor.DARK_PURPLE)));
    }
}
