package com.ancienty.anchypetrain.Logic;

import com.ancienty.anchypetrain.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuyPackage {

    public static boolean buyPackage(OfflinePlayer player, String package_name) {
        // Returns true if the package was found, false otherwise.
        package_name = package_name.toLowerCase(Locale.ENGLISH);
        FileConfiguration config = Main.getPlugin().getConfig();
        Date date;
        double price;
        boolean successful = false;
        if (config.getConfigurationSection("packages." + package_name) != null) {
            date = new Date();
            price = config.getDouble("packages." + package_name + ".price");
            Main.getPlugin().recent_purchases.add(new DataHistory(player, date, package_name.toUpperCase(Locale.ENGLISH), price));
            successful = true;
        }
        if (successful) {
            List<String> commands = config.getStringList("packages." + package_name + ".commands");
            for (String command : commands) {
                command = command.replace("{player}", player.getName());
                Main.getPlugin().getServer().dispatchCommand(Main.getPlugin().getServer().getConsoleSender(), command);
            }
            String message = Main.getMessage("successful_purchase");
            message = message.replace("{package}", package_name.toUpperCase());
            if (player.isOnline()) {
                ((Player) player).sendMessage(message);
            }
        }
        return successful;
    }
}
