package com.ancienty.anchypetrain;

import com.ancienty.anchypetrain.Commands.HypeTrainCommand;
import com.ancienty.anchypetrain.Logic.DataHistory;
import com.ancienty.anchypetrain.Logic.TrainController;
import com.ancienty.anchypetrain.Utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Main extends JavaPlugin {

    public List<DataHistory> recent_purchases = new ArrayList<>();
    public boolean is_train_active = false;
    public int train_level = 0;
    public double total_donations_in_price = 0;
    public int total_donations = 0;
    public int time_remaining = -1;
    public static Main plugin;
    public BossBar bossBar;
    private static Permission perms = null;
    public static YamlConfiguration lang;

    @Override
    public void onEnable() {
        // Plugin startup logic

        getLogger().info("ancHypeTrain is being activated.");
        plugin = this;
        getLogger().info("Reading config.yml");
        saveDefaultConfig();
        getLogger().info("Reading lang files.");
        try {createLangFiles();} catch (IOException e) {throw new RuntimeException(e);}
        getLogger().info("Hooking into vault.");
        if (!setupPermissions()) {
            getLogger().severe("Vault dependency could not be found.");
            getServer().getPluginManager().disablePlugin(this);
            Bukkit.getScheduler().cancelTasks(this);
            return;
        }
        getLogger().info("Creating commands.");
        new HypeTrainCommand();

        // Licensing (Disabled for SPIGOTMC!)
        Utils utils = new Utils();
        utils.checkLicense();

        bossBar = Bukkit.createBossBar("test", BarColor.valueOf(Main.getPlugin().getConfig().getString("train.bossbar-color")), BarStyle.valueOf(Main.getPlugin().getConfig().getString("train.bossbar-style").replace("NOTCHED", "SEGMENTED")));

        // Remaining time countdown logic:
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (time_remaining > -1) {
                time_remaining -= 1;
                if (is_train_active) {
                    String format = Main.getPlugin().getConfig().getString("train.format");
                    if (TrainController.doesLevelExist(train_level + 1)) {
                        format = format.replace("{level}", String.valueOf(train_level));
                        format = format.replace("{next_level}", String.valueOf(train_level + 1));
                        format = format.replace("{remaining_donations}", String.valueOf(TrainController.calculateRemainingDonations()));
                        format = format.replace("{time_remaining}", String.valueOf(time_remaining));
                    } else {
                        bossBar.removeAll();
                        TrainController.resetTrain();
                        return;
                    }
                    format = ChatColor.translateAlternateColorCodes('&', format);

                    if (Main.getPlugin().getConfig().getString("train.mode").equalsIgnoreCase("actionbar")) {
                        String finalFormat = format;
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(finalFormat));
                        });
                    } else if (Main.getPlugin().getConfig().getString("train.mode").equalsIgnoreCase("bossbar")) {
                        bossBar.setTitle(format);
                        bossBar.setProgress(Main.getPlugin().getConfig().getDouble("train.bossbar-progress") / 100);
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (!bossBar.getPlayers().contains(player)) {
                                bossBar.addPlayer(player);
                            }
                        });
                        bossBar.setVisible(true);
                    }
                }

                if (time_remaining == 0) {
                    String message = Main.getMessage("time_has_run_out");
                    message = message.replace("{level}", String.valueOf(train_level));
                    TrainController.resetTrain();
                    getServer().broadcastMessage(message);
                }
            }
        }, 0, 20);

        getLogger().info("ancHypeTrain is activated.");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void createLangFiles() throws IOException {
        File langEN = new File(getDataFolder(), "/lang/lang_en.yml");
        if (!langEN.exists()) {
            plugin.saveResource("lang/lang_en.yml", true);
        }
        File langTR = new File(getDataFolder(), "/lang/lang_tr.yml");
        if (!langTR.exists()) {
            plugin.saveResource("lang/lang_tr.yml", true);
        }

        String configLang = getConfig().getString("config.lang");
        if (configLang != null) {
            if (configLang.equalsIgnoreCase("tr")) {
                lang = YamlConfiguration.loadConfiguration(langTR);
            } else if (configLang.equalsIgnoreCase("en")) {
                lang = YamlConfiguration.loadConfiguration(langEN);
            }
        }
    }

    public static ItemStack getHead(String value) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }

    public static Main getPlugin() {
        return plugin;
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static String getMessage(String langKey) {
        String prefix = ChatColor.translateAlternateColorCodes('&', lang.getString("lang.prefix"));
        String message = ChatColor.translateAlternateColorCodes('&', lang.getString("lang." + langKey));
        return prefix + message;
    }
}
