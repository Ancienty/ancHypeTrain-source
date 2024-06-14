package com.ancienty.anchypetrain.GUIs;

import com.ancienty.anchypetrain.Logic.DataHistory;
import com.ancienty.anchypetrain.Logic.TrainController;
import com.ancienty.anchypetrain.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InformationGUI implements Listener {

    public static void openInventory(Player player) {
        String title = Main.getPlugin().lang.getString("menu.menu_name");
        int size = Main.getPlugin().lang.getInt("menu.row-size");
        Inventory inventory = Bukkit.createInventory(null, size * 9, ChatColor.translateAlternateColorCodes('&', title));

        // Main information item here:

        int item_slot = Main.getPlugin().lang.getInt("menu.items.info.slot");
        inventory.setItem(item_slot, getInformationItem());

        // Recent donations items here:

        ItemStack recent_donations_itemStack;
        int recent_purchases_size = Main.getPlugin().recent_purchases.size();
        List<Integer> slot_list = Main.getPlugin().lang.getIntegerList("menu.received-donations.slots");
        for (int i = recent_purchases_size - 1; i >= 0; i--) {
            DataHistory dataHistory = Main.getPlugin().recent_purchases.get(i);
            String recent_donations_item_name = ChatColor.translateAlternateColorCodes('&', Main.getPlugin().lang.getString("menu.received-donations.name")).replace("{player}", dataHistory.getPlayer().getName());
            List<String> recent_lore_of_item = Main.getPlugin().lang.getStringList("menu.received-donations.lore");
            List<String> recent_real_lore = new ArrayList<>();
            for (String text2 : recent_lore_of_item) {
                text2 = ChatColor.translateAlternateColorCodes('&', text2);
                text2 = text2.replace("{package_name}", dataHistory.getPackage_bought());
                Date date = dataHistory.getDate(); // Get the date object
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                String formattedDate = formatter.format(date);
                text2 = text2.replace("{date}", formattedDate);
                text2 = text2.replace("{price}", String.valueOf(dataHistory.getPrice()));
                recent_real_lore.add(text2);
            }
            recent_donations_itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta recent_donations_itemMeta = (SkullMeta) recent_donations_itemStack.getItemMeta();
            recent_donations_itemMeta.setOwningPlayer(dataHistory.getPlayer());
            recent_donations_itemMeta.setDisplayName(recent_donations_item_name);
            recent_donations_itemMeta.setLore(recent_real_lore);
            recent_donations_itemStack.setItemMeta(recent_donations_itemMeta);
            inventory.setItem(slot_list.get(0), recent_donations_itemStack);
            slot_list.remove(0);
        }

        // Filler item logic here:

        ItemStack filler_item = new ItemStack(Material.valueOf(Main.getPlugin().lang.getString("menu.filler-item.material")));
        for (int i = 0; i < size * 9; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType().equals(Material.AIR)) {
                inventory.setItem(i, filler_item);
            }
        }

        player.openInventory(inventory);

    }

    @EventHandler
    public void onMenuInteract(InventoryClickEvent e) {
        Player player = (Player) e.getView().getPlayer();
        String title = Main.getPlugin().lang.getString("menu.menu_name");
        if (e.getView().getTitle().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', title))) {
            e.setCancelled(true);
            if (e.getRawSlot() == Main.getPlugin().lang.getInt("menu.items.info.slot")) {
                e.getView().close();
                Main.getPlugin().getServer().dispatchCommand(player, Main.getPlugin().lang.getString("menu.items.info.run-command"));
            }
        }
    }

    public static ItemStack getInformationItem() {
        ItemStack itemStack;
        String material_name = Main.getPlugin().lang.getString("menu.items.info.material");
        if (material_name.startsWith("head-")) {
            material_name = material_name.replace("head-", "");
            itemStack = Main.getPlugin().getHead(material_name);
        } else {
            itemStack = new ItemStack(Material.valueOf(material_name));
        }
        String item_name = ChatColor.translateAlternateColorCodes('&', Main.getPlugin().lang.getString("menu.items.info.name"));
        List<String> lore_of_item = Main.getPlugin().lang.getStringList("menu.items.info.lore");
        List<String> real_lore = new ArrayList<>();
        for (String text : lore_of_item) {
            text = ChatColor.translateAlternateColorCodes('&', text);
            text = text.replace("{train_level}", String.valueOf(Main.getPlugin().train_level));
            text = text.replace("{total_donations}", String.valueOf(Main.getPlugin().total_donations_in_price));
            text = text.replace("{required_next}", String.valueOf(TrainController.calculateRemainingDonations()));
            if (text.contains("{next_level_rewards}")) {
                text = text.replace("{next_level_rewards}", "");
                real_lore.add(text);
                String nextLevelRewards = TrainController.getNextLevelRewards();
                real_lore.addAll(Arrays.asList(nextLevelRewards.split("\n")));
            } else {
                real_lore.add(text);
            }
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(item_name);
        itemMeta.setLore(real_lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
