package com.ancienty.anchypetrain.Utils;

import com.ancienty.anchypetrain.Main;
import org.bukkit.Bukkit;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

public class Utils {

    private static final String API_URL = "https://ancplugins.com/app/api/license.php?product=3&ip=";

    public void checkLicense() {
        try {
            String localIPAddress = getLocalIPAddress();
            if (localIPAddress == null) {
                Bukkit.getConsoleSender().sendMessage("IP Adresi belirlenemedi.");
                return;
            }

            JSONObject response = sendGET(API_URL + localIPAddress);

            if (response.getBoolean("status")) {
                Bukkit.getConsoleSender().sendMessage("Lisans bulundu!");
                Bukkit.getConsoleSender().sendMessage("Plugin aktif!");
            } else {
                Bukkit.getLogger().severe("Lisans bulunamadı, sunucu kapatılıyor.");
                Bukkit.getScheduler().cancelTasks(Main.getPlugin());
                Bukkit.shutdown();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private String getLocalIPAddress() throws SocketException {
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = (NetworkInterface) interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) {
                continue;
            }
            Enumeration addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = (InetAddress) addresses.nextElement();
                if (addr.isLinkLocalAddress() || addr.isLoopbackAddress() || addr.isMulticastAddress()) {
                    continue;
                }
                return addr.getHostAddress();
            }
        }
        return null;
    }

    private JSONObject sendGET(String url) throws IOException, JSONException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return new JSONObject(response.toString());
        } else {
            throw new IOException("Response code: " + responseCode);
        }
    }
}
