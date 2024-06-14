package com.ancienty.anchypetrain.Commands;

import com.ancienty.anchypetrain.GUIs.InformationGUI;
import com.ancienty.anchypetrain.Logic.BuyPackage;
import com.ancienty.anchypetrain.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HypeTrainCommand extends Command {

    public HypeTrainCommand() {
        super(
                "anchypetrain",
                new String[]{"aht", "hypetrain", "hype"},
                "The main command for ancHypeTrain",
                "anchypetrain.player");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
                InformationGUI.openInventory(player);
            } else if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
                if (Main.getPermissions().has(player, "anchypetrain.admin")) {
                    OfflinePlayer offlinePlayer = null;
                    try {
                        offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    } catch (Exception ex) {
                        String message = Main.getMessage("player_not_found");
                        player.sendMessage(message);
                    }
                    if (offlinePlayer != null) {
                        String package_name = args[2];
                        BuyPackage.buyPackage(offlinePlayer, package_name);
                        if (Main.getPlugin().getConfig().getBoolean("train.announce-messages")) {
                            String message = Main.getMessage("broadcast_purchase");
                            message = message.replace("{player}", offlinePlayer.getName());
                            message = message.replace("{package}", package_name.toUpperCase());
                            Main.getPlugin().getServer().broadcastMessage(message);
                        }
                    } else {
                        String message = Main.getMessage("player_not_found");
                        player.sendMessage(message);
                    }
                } else {
                    String message = Main.getMessage("no_permissions");
                    player.sendMessage(message);
                }
            } else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (Main.getPermissions().has(player, "anchypetrain.admin")) {
                    Main.getPlugin().reloadConfig();
                    Main.getPlugin().bossBar = Bukkit.createBossBar("test", BarColor.valueOf(Main.getPlugin().getConfig().getString("train.bossbar-color")), BarStyle.valueOf(Main.getPlugin().getConfig().getString("train.bossbar-style").replace("NOTCHED", "SEGMENTED")));
                    player.sendMessage("Config has been reloaded.");
                }
            } else if (args.length > 0 && args[0].equalsIgnoreCase("packages")) {
                List<String> packages = getPackages();
                StringBuilder package_list = new StringBuilder();
                for (String package_name : packages) {
                    package_list.append("&d").append(package_name).append("&7").append("\n");
                }
                String message = Main.getMessage("package_list");
                message = message.replace("{packages}", package_list.toString());
                message = ChatColor.translateAlternateColorCodes('&', message);
                player.sendMessage(message);
            } else {
                String message = Main.getMessage("info_command");
                player.sendMessage(message);
                if (Main.getPermissions().has(player, "anchypetrain.admin")) {
                    message = Main.getMessage("info_admin_command");
                    player.sendMessage(message);
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
                OfflinePlayer offlinePlayer = null;
                try {
                    offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                } catch (Exception ex) {
                    String message = Main.getMessage("player_not_found");
                    Bukkit.getConsoleSender().sendMessage(message);
                }
                if (offlinePlayer != null) {
                    String package_name = args[2];
                    BuyPackage.buyPackage(offlinePlayer, package_name);
                    String message = Main.getMessage("broadcast_purchase");
                    message = message.replace("{player}", offlinePlayer.getName());
                    message = message.replace("{package}", package_name.toUpperCase());
                    Main.getPlugin().getServer().broadcastMessage(message);
                } else {
                    String message = Main.getMessage("player_not_found");
                    Bukkit.getConsoleSender().sendMessage(message);
                }
            } else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                Main.getPlugin().reloadConfig();
                Main.getPlugin().bossBar = Bukkit.createBossBar("test", BarColor.valueOf(Main.getPlugin().getConfig().getString("train.bossbar-color")), BarStyle.valueOf(Main.getPlugin().getConfig().getString("train.bossbar-style").replace("NOTCHED", "SEGMENTED")));
                Bukkit.getConsoleSender().sendMessage("Config has been reloaded.");
            } else {
                Bukkit.getConsoleSender().sendMessage("This command does not exist.");
            }
        }
    }

    public List<String> getPackages() {
        return new ArrayList<>(Main.getPlugin().getConfig().getConfigurationSection("packages").getKeys(false));
    }
}
