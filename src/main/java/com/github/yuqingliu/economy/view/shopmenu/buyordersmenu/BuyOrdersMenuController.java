package com.github.yuqingliu.economy.view.shopmenu.buyordersmenu;

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
public class BuyOrdersMenuController extends AbstractPlayerInventoryController<ShopMenu> {
    private final int[] prevMenuButton = new int[]{1,2};
    private final int[] exitMenuButton = new int[]{1,3};
    private final int[] reloadButton = new int[]{1,4};
    private final int[] nextBuyOrdersButton = new int[]{7,2};
    private final int[] prevBuyOrdersButton = new int[]{7,3};
    private final int[] buyOrdersStart = new int[]{2,1};
    private final int buyOrdersLength = 5;
    private final int buyOrdersWidth = 4;
    private final int buyOrdersSize = buyOrdersLength * buyOrdersWidth;
    private final List<int[]> buyOrders;
    private final PageData<ShopOrderEntity> pageData = new PageData<>();
    
    public BuyOrdersMenuController(Player player, Inventory inventory, ShopMenu shopMenu) {
        super(player, inventory, shopMenu);
        this.buyOrders = rectangleArea(buyOrdersStart, buyOrdersWidth, buyOrdersLength);
    }

    public void openMenu() {
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.BuyOrdersMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        reload();
    }

    public void reload() {
        rectangleAreaLoading(buyOrdersStart, buyOrdersWidth, buyOrdersLength);
        Scheduler.runAsync((task) -> {
            fetchBuyOrders();
            displayBuyOrdersOptions();
        });
    }

    public void nextBuyOrdersPage() {
        pageData.nextPage(() -> displayBuyOrdersOptions());
    }

    public void prevBuyOrdersPage() {
        pageData.prevPage(() -> displayBuyOrdersOptions());
    }

    private void fetchBuyOrders() {
        pageData.clear();
        List<ShopOrderEntity> buyOrders = menu.getShopService().getPlayerBuyOrders(player);
        Queue<ShopOrderEntity> tempBuyOrders = new ArrayDeque<>();
        tempBuyOrders.addAll(buyOrders);
        int maxBuyOrdersPages = (int) Math.ceil((double) tempBuyOrders.size() / (double) buyOrdersSize);
        for (int i = 0; i < maxBuyOrdersPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, ShopOrderEntity> options = new LinkedHashMap<>();
            for (int[] coords : this.buyOrders) {
                if(tempBuyOrders.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), tempBuyOrders.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayBuyOrdersOptions() {
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
                    meta.displayName(Component.text("BUY ORDER", NamedTextColor.GOLD));
                    Component nameComponent = Component.text(order.getItemName(), NamedTextColor.AQUA);
                    Component currencyComponent = Component.text("Currency: ", NamedTextColor.BLUE).append(Component.text(order.getCurrencyType(), NamedTextColor.GOLD));
                    Component priceComponent = Component.text("Unit Buy Price: ", NamedTextColor.BLUE).append(Component.text(order.getUnitPrice() + "$/unit", NamedTextColor.GOLD));
                    Component quantityComponent = Component.text("Quantity: ", NamedTextColor.BLUE).append(Component.text(order.getQuantity() + "x", NamedTextColor.GREEN));
                    Component quantityBoughtComponent = Component.text("Quantity Bought: ", NamedTextColor.BLUE).append(Component.text(order.getFilledQuantity() + "x", NamedTextColor.GREEN));
                    meta.lore(Arrays.asList(nameComponent, currencyComponent, priceComponent, quantityComponent, quantityBoughtComponent));
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
        setItem(nextBuyOrdersButton, getNextPageIcon());
        setItem(prevBuyOrdersButton, getPrevPageIcon());
        setItem(reloadButton, getReloadIcon());
    }
}
