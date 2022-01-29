package com.johnymuffin.beta.legacyminecraft.pinger;

import com.johnymuffin.beta.legacyminecraft.pinger.config.ConfigurationFile;
import com.johnymuffin.beta.legacyminecraft.pinger.config.JSONConfiguration;
import com.johnymuffin.beta.legacyminecraft.pinger.config.YMLConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

public class LegacyMinecraftPinger extends JavaPlugin {
    //Basic Plugin Info
    private static LegacyMinecraftPinger plugin;
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private Integer taskID;
    private String serverIcon;

    private ConfigurationFile LMPConfig;


    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();
        log.info("[" + pluginName + "] Is Loading, Version: " + pdf.getVersion());

        //Generate Config
        File configFile;
        boolean newConfig;
        try {
            configFile = new File(this.getDataFolder(), "config.yml");
            newConfig = !configFile.exists();
            LMPConfig = new YMLConfiguration(configFile);
        } catch (Exception exception) {
            logger(Level.WARNING, "YML Configuration file mode failed. Falling back to JSON.");
            configFile = new File(this.getDataFolder(), "config.json");
            newConfig = !configFile.exists();
            LMPConfig = new JSONConfiguration(configFile);
        }

        LMPConfig.load();

        LMPConfig.generateConfigOption("config-version", 1);
        LMPConfig.generateConfigOption("url", "https://servers.api.legacyminecraft.com/api/v1/serverPing");
        LMPConfig.generateConfigOption("serverName", "My Test Server");
        LMPConfig.generateConfigOption("description", "My server is pretty nice, you should check it out!");
        LMPConfig.generateConfigOption("version", "B1.7.3");
        LMPConfig.generateConfigOption("serverIP", "mc.retromc.org");
        LMPConfig.generateConfigOption("serverPort", Bukkit.getServer().getPort());
        LMPConfig.generateConfigOption("onlineMode", Bukkit.getServer().getOnlineMode());
        LMPConfig.generateConfigOption("serverOwner", "ThatGuy");
        LMPConfig.generateConfigOption("pingTime", 45);
        LMPConfig.generateConfigOption("maxPlayers", Bukkit.getServer().getMaxPlayers());
        LMPConfig.generateConfigOption("key.info", "A key is required to list your server on the Legacy Minecraft server list. Please contact Johny Muffin#9406 on Discord for a key, or email legacykey@johnymuffin.com to get one.");
        LMPConfig.generateConfigOption("key.value", "");
        LMPConfig.generateConfigOption("debug", false);

        LMPConfig.generateConfigOption("flags.BetaEvolutions.enabled", false);
        LMPConfig.generateConfigOption("flags.BetaEvolutions.info", "Enabled this if your server runs Beta Evolutions");
        LMPConfig.generateConfigOption("flags.MineOnline.enabled", true);
        LMPConfig.generateConfigOption("flags.MineOnline.info", "Enable this flag if you want your server to be listed on the MineOnline launcher.");

        LMPConfig.writeConfigurationFile();


        if (newConfig) {
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
            final String apiURL = LMPConfig.getConfigString("url");
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

                    try {
                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonResponse = (JSONObject) jsonParser.parse(response.toString());
                        if (jsonResponse.containsKey("notice")) {
                            plugin.logger(Level.INFO, "Message from API: " + String.valueOf(jsonResponse.get("notice")));
                        }

                    } catch (Exception e) {
                        plugin.logger(Level.INFO, "Malformed JSON response after ping returned normal status code: " + e + ": " + e.getMessage());
                        return;
                    }

                    rd.close();
                } catch (Exception e) {
                    plugin.logger(Level.WARNING, "An error occurred when attempting to ping: " + e + ": " + e.getMessage());
                    if (this.LMPConfig.getConfigBoolean("debug")) {
                        plugin.logger(Level.WARNING, "Ping Object: " + jsonData);
                    }
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }, 0L);

        }, 20, 20 * Integer.valueOf(String.valueOf(LMPConfig.getConfigOption("pingTime", 45))));


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
        tmp.put("name", LMPConfig.getConfigString("serverName"));
        tmp.put("description", LMPConfig.getConfigString("description"));
        tmp.put("version", LMPConfig.getConfigString("version"));
        tmp.put("ip", LMPConfig.getConfigString("serverIP"));
        tmp.put("port", LMPConfig.getConfigInteger("serverPort"));
        tmp.put("onlineMode", LMPConfig.getConfigBoolean("onlineMode"));
        tmp.put("maxPlayers", LMPConfig.getConfigString("maxPlayers"));
        tmp.put("key", LMPConfig.getConfigString("key.value"));
        JSONArray playerArray = new JSONArray();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            JSONObject playerData = new JSONObject();
            playerData.put("username", p.getName());
            playerData.put("uuid", generateOfflineUUID(p.getName()).toString());
            playerData.put("x", p.getLocation().getX());
            playerData.put("y", p.getLocation().getY());
            playerData.put("z", p.getLocation().getZ());
            playerData.put("world", p.getLocation().getWorld().getName());
            //TODO: Seconds Online
            playerData.put("secondsOnline", 0);
            playerArray.add(playerData);
        }
        tmp.put("players", playerArray);
        tmp.put("playersOnline", playerArray.size());
        //Flags - Start
        JSONArray flags = new JSONArray();
        JSONObject betaEVOFlag = new JSONObject();
        betaEVOFlag.put("enabled", LMPConfig.getConfigBoolean("flags.BetaEvolutions.enabled"));
        betaEVOFlag.put("name", "BetaEvolutions");
        flags.add(betaEVOFlag);
        JSONObject mineOnlineFlag = new JSONObject();
        mineOnlineFlag.put("name", "MineOnline");
        mineOnlineFlag.put("enabled", LMPConfig.getConfigBoolean("flags.MineOnline.enabled"));
        flags.add(mineOnlineFlag);
        //Flags - End
        tmp.put("flags", flags);
        tmp.put("protocol", 1);
        tmp.put("pluginName", pluginName);
        tmp.put("pluginVersion", pdf.getVersion());
        if (serverIcon != null) {
            tmp.put("serverIcon", serverIcon);
        }
        tmp.put("edition", "1.2.5");
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
