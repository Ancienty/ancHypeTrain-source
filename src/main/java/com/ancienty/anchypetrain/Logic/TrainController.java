package com.ancienty.anchypetrain.Logic;


import com.ancienty.anchypetrain.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrainController {

    public static boolean doesLevelExist(int check_level) {
        if (Main.getPlugin().getConfig().getConfigurationSection("levels." + check_level) != null) {
            return true;
        } else {
            return false;
        }
    }

    public static void checkTrain(DataHistory new_purchase) {
        Main.getPlugin().total_donations += 1;
        Main.getPlugin().total_donations_in_price += new_purchase.getPrice();
        Main.getPlugin().time_remaining = Main.getPlugin().getConfig().getInt("train.time");
        if (!Main.getPlugin().is_train_active) {
            if (Main.getPlugin().total_donations_in_price >= Main.getPlugin().getConfig().getDouble("levels.1.threshold")) {
                activateTrain(new_purchase);
            }
        }

        int level_should_be = trainLevelShouldBe();
        if (level_should_be > Main.getPlugin().train_level) {
            Main.getPlugin().train_level = level_should_be;
            giveRewards(new_purchase, level_should_be);
        }
    }

    public static double calculateRemainingDonations() {
        int next_level = Main.getPlugin().train_level + 1;
        if (doesLevelExist(next_level)) {
            double donations_should_be = Main.getPlugin().getConfig().getDouble("levels." + next_level + ".threshold");
            double remaining = donations_should_be - Main.getPlugin().total_donations_in_price;
            return (double) Math.round(remaining * 100) / 100;
        } else {
            return 99999.99;
        }
    }

    public static void resetTrain() {
        Main.getPlugin().is_train_active = false;
        Main.getPlugin().train_level = 0;
        Main.getPlugin().total_donations = 0;
        Main.getPlugin().total_donations_in_price = 0;
        Main.getPlugin().time_remaining = -1;
        Main.getPlugin().recent_purchases = new ArrayList<>();
        Main.getPlugin().bossBar.removeAll();
        // Broadcast train has ended message, check if broadcast is true first.
    }

    public static void activateTrain(DataHistory last_purchase) {
        Main.getPlugin().train_level = 1;
        Main.getPlugin().is_train_active = true;
        Main.getPlugin().time_remaining = Main.getPlugin().getConfig().getInt("train.time");
        giveRewards(last_purchase, 1);
        // Broadcast train has started message, check if broadcast is true first.
    }

    public static int trainLevelShouldBe() {
        double total_donations = Main.getPlugin().total_donations_in_price;
        int level_to_return = Main.getPlugin().train_level;
        for (int i = 0; i <= 1000; i++) {
            if (Main.getPlugin().getConfig().getConfigurationSection("levels." + i) != null && total_donations >= Main.getPlugin().getConfig().getDouble("levels." + i + ".threshold")) {
                level_to_return = i;
            }
        }
        return level_to_return;
    }

    public static void giveRewards(DataHistory last_purchase, int level) {
        if (Main.getPlugin().getConfig().getBoolean("train.announce-messages")) {
            String message = Main.getMessage("giving_out_rewards");
            message = message.replace("{level}", String.valueOf(level));
            Main.getPlugin().getServer().broadcastMessage(message);
        }
        int delay = Main.getPlugin().getConfig().getInt("train.delay");
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (doesLevelExist(level)) {
                List<String> commands = Main.getPlugin().getConfig().getStringList("levels." + level + ".rewards.commands");
                String reward_type = Main.getPlugin().getConfig().getString("levels." + level + ".rewards.type");
                String random_interval = Main.getPlugin().getConfig().getString("levels." + level + ".rewards.random");

                if (reward_type.equalsIgnoreCase("everyone")) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        for (String command : commands) {
                            command = command.replace("{player}", player.getName());
                            Main.getPlugin().getServer().dispatchCommand(Main.getPlugin().getServer().getConsoleSender(), command);
                        }
                    });
                } else if (reward_type.equalsIgnoreCase("donator")) {
                    for (String command : commands) {
                        command = command.replace("{player}", last_purchase.getPlayer().getName());
                        Main.getPlugin().getServer().dispatchCommand(Main.getPlugin().getServer().getConsoleSender(), command);
                    }
                } else if (reward_type.equalsIgnoreCase("random")) {
                    try {
                        int random_interval_1 = Integer.valueOf(random_interval.split("-")[0]);
                        int random_interval_2 = Integer.valueOf(random_interval.split("-")[1]);
                        Random random = new Random();
                        int random_from_random = random.nextInt(random_interval_2 - random_interval_1);
                        int real_random = random_from_random + random_interval_1;
                        List<OfflinePlayer> already_given_rewards = new ArrayList<>();
                        int size_of_players = Bukkit.getOnlinePlayers().size();
                        if (real_random > size_of_players) {
                            real_random = size_of_players;
                        }


                        for (int i = 1; i <= real_random; i++) {
                            int rand = random.nextInt(size_of_players);
                            OfflinePlayer offlinePlayer = (OfflinePlayer) Bukkit.getOnlinePlayers().toArray()[rand];
                            if (!already_given_rewards.contains(offlinePlayer)) {
                                already_given_rewards.add(offlinePlayer);
                                for (String command : commands) {
                                    command = command.replace("{player}", offlinePlayer.getName());
                                    Main.getPlugin().getServer().dispatchCommand(Main.getPlugin().getServer().getConsoleSender(), command);
                                }
                            } else {
                                i -= 1;
                            }
                        }
                    } catch (NumberFormatException ex) {
                        Main.getPlugin().getLogger().severe("ERROR! The random interval is not set correctly for train level '" + level + "' - please fix this configuration error ASAP!");
                    }
                } else {
                    Main.getPlugin().getLogger().severe("ERROR! Could not determine the reward type for train level '" + level + "' - please fix this configuration error ASAP!");
                }
            }
        }, delay * 20L);
    }


    public static String getNextLevelRewards() {
        StringBuilder builder = new StringBuilder();
        int next_level = Main.getPlugin().train_level + 1;
        if (doesLevelExist(next_level)) {
            List<String> stringList = Main.getPlugin().getConfig().getStringList("levels." + next_level + ".rewards.description");
            for (String text : stringList) {
                builder.append(ChatColor.translateAlternateColorCodes('&', text)).append("\n");
            }
            return builder.toString();
        } else {
            return "FINAL LEVEL REACHED";
        }
    }
}
