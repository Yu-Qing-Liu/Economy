package com.github.yuqingliu.economy.view.vendormenu.transactionmenu;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.yuqingliu.economy.api.Scheduler;
import com.github.yuqingliu.economy.persistence.entities.VendorItemEntity;
import com.github.yuqingliu.economy.view.AbstractPlayerInventoryController;
import com.github.yuqingliu.economy.view.PageData;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu;
import com.github.yuqingliu.economy.view.vendormenu.VendorMenu.MenuType;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class TransactionMenuController extends AbstractPlayerInventoryController<VendorMenu> {
    private final int[] prevOptionsButton = new int[]{1,3};
    private final int[] nextOptionsButton = new int[]{1,4};
    private final int[] prevMenuButton = new int[]{7,3};
    private final int[] exitMenuButton = new int[]{7,4};
    private final int[] itemSlot = new int[]{4,1};
    private final int optionsLength = 5;
    private final int optionsWidth = 2;
    private final int optionsSize = optionsLength * optionsWidth;
    private final int[] optionsStart = new int[]{2,3};
    private final List<int[]> currencyOptions;
    private final PageData<CurrencyOption> pageData = new PageData<>();
    private VendorItemEntity item;
    
    public TransactionMenuController(Player player, Inventory inventory, VendorMenu vendorMenu) {
        super(player, inventory, vendorMenu);
        this.currencyOptions = rectangleArea(optionsStart, optionsWidth, optionsLength);
    }

    public void openMenu(VendorItemEntity item) {
        this.item = item;
        Scheduler.runLaterAsync((task) -> {
            menu.getPlayerMenuTypes().put(player, MenuType.TransactionMenu);
        }, Duration.ofMillis(50));
        fill(getBackgroundTile(Material.BLUE_STAINED_GLASS_PANE));
        border();
        buttons();
        displayItem();
        rectangleAreaLoading(optionsStart, optionsWidth, optionsLength);
        Scheduler.runAsync((task) -> {
            fetchOptions();
            displayOptions();
        });
    }

    public void nextPage() {
        pageData.nextPage(() -> displayOptions());
    }

    public void prevPage() {
        pageData.prevPage(() -> displayOptions());
    }

    private void displayItem() {
        setItem(itemSlot, item.getIcon().clone());
    }

    private void border() {
        ItemStack borderItem = createSlotItem(Material.BLACK_STAINED_GLASS_PANE, getUnavailableComponent());
        fillRectangleArea(new int[]{1,0}, 1, 7, borderItem);
        fillRectangleArea(new int[]{1,1}, 1, 3, borderItem);
        fillRectangleArea(new int[]{5,1}, 1, 3, borderItem);
        fillRectangleArea(new int[]{1,2}, 1, 7, borderItem);
        fillRectangleArea(new int[]{1,5}, 1, 7, borderItem);
    }

    private void buttons() {
        setItem(prevOptionsButton, getPrevPageIcon());
        setItem(nextOptionsButton, getNextPageIcon());
        setItem(prevMenuButton, getPrevMenuIcon());
        setItem(exitMenuButton, getExitMenuIcon());
    }

    private void fetchOptions() {
        Map<String, Double> buyPrices = item.getBuyPrices();
        Map<String, Double> sellPrices = item.getSellPrices();
        Queue<CurrencyOption> temp = new ArrayDeque<>();
        for(Map.Entry<String, Double> entry : buyPrices.entrySet()) {
            String currencyName = entry.getKey();
            double buyPrice = entry.getValue();
            double sellPrice = sellPrices.get(currencyName);
            ItemStack icon =  menu.getCurrencyService().getCurrencyByName(currencyName).getIcon().clone();
            CurrencyOption option = new CurrencyOption(icon, buyPrice, sellPrice);
            temp.offer(option);
        }
        int maxPages = (int) Math.ceil((double) temp.size() / (double) optionsSize);
        for (int i = 0; i < maxPages; i++) {
            int pageNum = i + 1;
            Map<List<Integer>, CurrencyOption> options = new LinkedHashMap<>();
            for (int[] coords : currencyOptions) {
                if(temp.isEmpty()) {
                    options.put(Arrays.asList(coords[0], coords[1]), null);
                } else {
                    options.put(Arrays.asList(coords[0], coords[1]), temp.poll());
                }
            }
            pageData.put(pageNum, options);
        }
    }

    private void displayOptions() {
        Map<List<Integer>, CurrencyOption> options = pageData.getCurrentPageData();
        for(Map.Entry<List<Integer>, CurrencyOption> entry : options.entrySet()) {
            List<Integer> coords = entry.getKey();
            CurrencyOption option = entry.getValue();
            if(option == null) {
                setItem(coords, getUnavailableIcon());
            } else {
                ItemStack item = option.getIcon().clone();
                ItemMeta itemMeta = item.getItemMeta();
                Component buyPrice = Component.text("UNIT BUY PRICE: ", NamedTextColor.DARK_AQUA).append(Component.text(option.getBuyPrice() +"$ ", NamedTextColor.DARK_GREEN));
                Component sellPrice = Component.text("UNIT SELL PRICE: ", NamedTextColor.DARK_AQUA).append(Component.text(option.getSellPrice() +"$ ", NamedTextColor.DARK_GREEN));
                itemMeta.lore(Arrays.asList(buyPrice, sellPrice));
                item.setItemMeta(itemMeta);
                setItem(coords, item);
            }
        }
    }
}
