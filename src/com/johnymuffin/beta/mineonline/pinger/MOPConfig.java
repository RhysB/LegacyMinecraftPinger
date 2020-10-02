package com.johnymuffin.beta.mineonline.pinger;

import org.bukkit.Bukkit;
import org.bukkit.util.config.Configuration;

import java.io.File;

public class MOPConfig extends Configuration {
    private boolean isNew = true;


    public MOPConfig(File file) {
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
        generateConfigOption("url", "http://mineonline.codie.gg/api/servers");
        generateConfigOption("serverName", "My Test Server");
        generateConfigOption("serverIP", "mc.minecraft.test");
        generateConfigOption("serverOwner", "ThatGuy");
        generateConfigOption("onlineMode", true);
        generateConfigOption("port", Bukkit.getServer().getPort());
        generateConfigOption("pingTime", 45);
        generateConfigOption("version-md5", "CC263AA969F2D8621C5443A5A18897E2");
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
