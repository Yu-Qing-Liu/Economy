package com.github.yuqingliu.economy.view.shopmenu.sellordersmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.ShopOrderEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.PageData;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu;
import com.github.yuqingliu.economy.view.shopmenu.ShopMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class SellOrdersMenuController extends AbstractPlayerInventoryController<ShopMenu> {
    private final int[] prevMenuButton = new int[]{1,2};
    private final int[] exitMenuButton = new int[]{1,3};
    private final int[] reloadButton = new int[]{1,4};
    private final int[] nextSellOrdersButton = new int[]{7,2};
    private final int[] prevSellOrdersButton = new int[]{7,3};
    private final int[] sellOrdersStart = new int[]{2,1};
    private final int sellOrdersLength = 5;
    private final int sellOrdersWidth = 4;
    private final int sellOrdersSize = sellOrdersLength * sellOrdersWidth;
    private final List<int[]> sellOrders;
    private final PageData<ShopOrderEntity> pageData = new PageData<>();
    
    public SellOrdersMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
        this.sellOrders = rectangleArea(sellOrdersStart, sellOrdersWidth, sellOrdersLength);
    }

    public void openMenu() {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.SellOrdersMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        reload();
    }

    public void reload() {
        rectangleAreaLoading(sellOrdersStart, sellOrdersWidth, sellOrdersLength);
        Scheduler.runAsync((task) -> {
            fetchSellOrders();
            displaySellOrdersOptions();
        });
    }

    public void nextPage() {
        pageData.nextPage(() -> displaySellOrdersOptions());    
    }

    public void prevPage() {
        pageData.prevPage(() -> displaySellOrdersOptions());    
    }

    private void fetchSellOrders() {
        pageData.clear();
        List<ShopOrderEntity> sellOrders = menu.getShopService().getPlayerSellOrders(player);
        Queue<ShopOrderEntity> tempSellOrders = new ArrayDeque<>();
        tempSellOrders.addAll(sellOrders);
        int maxSellOrdersPages = (int) Math.ceil((double) tempSellOrders.size() / (double) sellOrdersSize);
        for (int i = 0; i < maxSellOrdersPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, ShopOrderEntity> options = new LinkedHashMap<>();
            for (int[] coords : this.sellOrders) {
                if(tempSellOrders.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), tempSellOrders.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displaySellOrdersOptions() {
        Map<List<Integer>, ShopOrderEntity> orders = pageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, ShopOrderEntity> entry : orders.entrySet()) {
            List<Integer> coords = entry.getKey();
            ShopOrderEntity order = entry.getValue();
            if(order == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack orderIcon = order.getShopItem().getIcon().clone();
                ItemMeta meta = orderIcon.getItemMeta();
                if(meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    meta.displayName(Component.text("SELL ORDER", NamedTextColor.GOLD));
                    Component nameComponent = Component.text(order.getItemName(), NamedTextColor.AQUA);
                    Component priceComponent = Component.text("Unit Sell Price: ", NamedTextColor.BLUE).append(Component.text(String.format("%.2f ", order.getUnitPrice()), NamedTextColor.DARK_GREEN)).append(Component.text(String.format("%s/unit", order.getCurrencyType()), NamedTextColor.GOLD));
                    Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() + "x", NamedTextColor.GREEN));
                    Component quantityBoughtComponent = Component.text("Quantity Sold: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x", NamedTextColor.GREEN));
                    meta.lore(Arrays.asList(nameComponent, priceComponent, quantityComponent, quantityBoughtComponent));
                }
                orderIcon.setItemMeta(meta);
                setItem(coords, orderIcon);
            }
        }
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        int[] b1 = new int[]{1,1};
        int[] b3 = new int[]{7,1};
        int[] b4 = new int[]{7,4};
        setItem(b1, borderItem);
        setItem(b3, borderItem);
        setItem(b4, borderItem);
    }

    private void buttons() {
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
        setItem(nextSellOrdersButton, getNextPageIcon());
        setItem(prevSellOrdersButton, getPrevPageIcon());
        setItem(reloadButton, getReloadIcon());
    }
}
