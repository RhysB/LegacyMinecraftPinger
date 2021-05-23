package com.johnymuffin.beta.mineonline.pinger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MineOnlinePinger extends JavaPlugin {
    //Basic Plugin Info
    private static MineOnlinePinger plugin;
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private MOPConfig mopConfig;
    private Integer taskID;
    private String serverIcon;


    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();
        log.info("[" + pluginName + "] Is Loading, Version: " + pdf.getVersion());
        this.mopConfig = new MOPConfig(new File(this.getDataFolder(), "config.yml"));
        if (mopConfig.isNew()) {
            logger(Level.WARNING, "Stopping the plugin as the config needs to be set correctly.");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Load Image
        File serverIconFile = new File(plugin.getDataFolder(), "server-icon.png");
        if (serverIconFile.exists()) {
            logger(Level.INFO, "Loading Server Icon into cache.");
            serverIcon = loadIcon(serverIconFile);
            if (serverIcon != null) {
                logger(Level.INFO, "Server Icon has been loaded into cache.");
            }
        }


        taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            final String jsonData = generateJsonData().toJSONString();
            final String apiURL = mopConfig.getConfigString("url");
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
                //Post code directly copied from: https://github.com/codieradical/MineOnlineBroadcast-Bukkit/blob/master/src/MineOnlineBroadcast.java
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(apiURL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    connection.getOutputStream().write(jsonData.getBytes(StandardCharsets.UTF_8));
                    connection.getOutputStream().flush();
                    connection.getOutputStream().close();

                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    System.out.println(response);

                    rd.close();
                } catch (Exception e) {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        plugin.logger(Level.INFO, "An error occurred when attempting to ping: " + e + ": " + e.getMessage());
                    }, 0L);
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }, 0L);

        }, 20, 20 * Integer.valueOf(String.valueOf(mopConfig.getConfigOption("pingTime", 45))));


    }

    @Override
    public void onDisable() {
        logger(Level.INFO, "Disabling.");
        if (taskID != null) {
            Bukkit.getServer().getScheduler().cancelTask(taskID);
        }
    }

    public void logger(Level level, String message) {
        Bukkit.getServer().getLogger().log(level, "[" + pluginName + "] " + message);
    }

    public JSONObject generateJsonData() {
        JSONObject tmp = new JSONObject();
        tmp.put("name", mopConfig.getConfigString("serverName"));
        tmp.put("description", mopConfig.getConfigString("description"));
        tmp.put("version", mopConfig.getConfigString("version"));
        tmp.put("ip", mopConfig.getConfigString("serverIP"));
        tmp.put("port", mopConfig.getConfigInteger("serverPort"));
        tmp.put("onlineMode", mopConfig.getConfigBoolean("onlineMode"));
        tmp.put("maxPlayers", mopConfig.getConfigString("maxPlayers"));
        tmp.put("key", mopConfig.getConfigString("key.value"));
        JSONArray playerArray = new JSONArray();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            JSONObject playerData = new JSONObject();
            playerData.put("username", p.getName());
            playerData.put("uuid", generateOfflineUUID(p.getName()).toString());
            playerData.put("x", p.getLocation().getX());
            playerData.put("y", p.getLocation().getY());
            playerData.put("z", p.getLocation().getZ());
            playerData.put("world", p.getLocation().getWorld());
            //TODO: Seconds Online
            playerData.put("secondsOnline", 0);
        }
        tmp.put("players", playerArray);
        tmp.put("playersOnline", playerArray.size());
        //Flags - Start
        JSONArray flags = new JSONArray();
        JSONObject betaEVOFlag = new JSONObject();
        betaEVOFlag.put("enabled", mopConfig.getConfigBoolean("flags.BetaEvolutions.enabled"));
        betaEVOFlag.put("name", "BetaEvolutions");
        flags.add(betaEVOFlag);
        JSONObject mineOnlineFlag = new JSONObject();
        mineOnlineFlag.put("name", "MineOnline");
        mineOnlineFlag.put("enabled", mopConfig.getConfigBoolean("flags.MineOnline.enabled"));
        flags.add(mineOnlineFlag);
        //Flags - End
        tmp.put("flags", flags);
        tmp.put("protocol", 1);
        if (serverIcon != null) {
            tmp.put("serverIcon", serverIcon);
        }
        System.out.println(tmp.toJSONString());
        return tmp;
    }

    public static UUID generateOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(username.getBytes());
    }

    private String loadIcon(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (!(bufferedImage.getHeight() == 64 && bufferedImage.getWidth() == 64)) {
                logger(Level.INFO, "The server-icon image has invalid dimensions, " + bufferedImage.getHeight() + "x" + bufferedImage.getWidth() + ". Try 64x64");
                return null;
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", output);
            String base64String = DatatypeConverter.printBase64Binary(output.toByteArray());
            base64String = base64String.replace("\n", "");
            return base64String;
        } catch (Exception e) {
            log.log(Level.WARNING, "An error occurred reading the server-icon", e);
        }
        return null;

    }


}
