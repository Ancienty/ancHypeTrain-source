package com.ancienty.anchypetrain.Logic;

import org.bukkit.OfflinePlayer;

import java.util.Date;

public class DataHistory {

    private OfflinePlayer player;
    private Date date;
    private String package_bought;
    private double price;

    public DataHistory(OfflinePlayer player, Date date, String package_bought, double price) {
        this.player = player;
        this.date = date;
        this.package_bought = package_bought;
        this.price = price;
        TrainController.checkTrain(this);
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public Date getDate() {
        return date;
    }

    public String getPackage_bought() {
        return package_bought;
    }

    public double getPrice() {
        return price;
    }
}
