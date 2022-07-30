package com.johnymuffin.beta.legacyminecraft.pinger.config;

import org.bukkit.util.config.Configuration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class YMLConfiguration extends Configuration implements ConfigurationFile{
    protected File configFile;
    private boolean fileChanged = false;

    public YMLConfiguration(File file) {
        super(file);
    }

    public void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            fileChanged = true;
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    public void writeConfigOption(String key, Object value) {
        this.setProperty(key, value);
        this.fileChanged = true;
    }

    @Override
    public void writeConfigurationFile() {
        if(fileChanged) {
            save();
        }
    }

    public Object getConfigOption(String key, Object obj) {
        if(getConfigOption(key) == null) {
            return obj;
        }
        return getConfigOption(key);
    }


    //Getters Start
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

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

    public Long getConfigLongOption(String key) {
        if (this.getConfigOption(key) == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(this.getProperty(key)));
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
        fileChanged = true;
        this.removeProperty(oldKey);
        return true;

    }


}