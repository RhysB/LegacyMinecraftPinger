package com.johnymuffin.beta.legacyminecraft.pinger;

import org.bukkit.Bukkit;
import org.bukkit.util.config.Configuration;

import java.io.File;

public class LMPConfig extends Configuration {
    private boolean isNew = true;


    public LMPConfig(File file) {
        super(file);
        this.isNew = !file.exists();
        reload();
    }

    public void reload() {
        this.load();
        this.write();
        this.save();
    }

    private void write() {
        if (this.getString("config-version") == null) {
            System.out.println("Converting to Config Version 1");
            convertToNewConfig();
        }
        generateConfigOption("config-version", 1);
        generateConfigOption("url", "https://servers.api.legacyminecraft.com/api/v1/serverPing");
        generateConfigOption("serverName", "My Test Server");
        generateConfigOption("description", "My server is pretty nice, you should check it out!");
        generateConfigOption("version", "B1.7.3");
        generateConfigOption("serverIP", "mc.retromc.org");
        generateConfigOption("serverPort", Bukkit.getServer().getPort());
        generateConfigOption("onlineMode", Bukkit.getServer().getOnlineMode());
        generateConfigOption("serverOwner", "ThatGuy");
        generateConfigOption("pingTime", 45);
        generateConfigOption("maxPlayers", Bukkit.getServer().getMaxPlayers());
        generateConfigOption("key.info", "A key is required to list your server on the Legacy Minecraft server list. Please contact Johny Muffin#9406 on Discord for a key, or email legacykey@johnymuffin.com to get one.");
        generateConfigOption("key.value", "");

        generateConfigOption("flags.BetaEvolutions.enabled", false);
        generateConfigOption("flags.BetaEvolutions.info", "Enabled this if your server runs Beta Evolutions");
        generateConfigOption("flags.MineOnline.enabled", true);
        generateConfigOption("flags.MineOnline.info", "Enable this flag if you want your server to be listed on the MineOnline launcher.");

    }


    private void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

    public Object getConfigOption(String key, Object defaultValue) {
        Object value = getConfigOption(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;

    }

    //Getters Start

    public String getConfigString(String key) {
        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key) {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key) {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key) {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }


    //Getters End

    private synchronized void convertToNewConfig() {

    }

    private boolean convertToNewAddress(String newKey, String oldKey) {
        if (this.getString(newKey) != null) {
            return false;
        }
        if (this.getString(oldKey) == null) {
            return false;
        }
        System.out.println("Converting Config: " + oldKey + " to " + newKey);
        Object value = this.getProperty(oldKey);
        this.setProperty(newKey, value);
        this.removeProperty(oldKey);
        return true;
    }

    public boolean isNew() {
        return isNew;
    }
}
