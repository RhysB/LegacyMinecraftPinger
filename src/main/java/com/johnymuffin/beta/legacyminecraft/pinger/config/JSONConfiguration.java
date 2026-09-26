package com.johnymuffin.beta.legacyminecraft.pinger.config;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONConfiguration implements ConfigurationFile {
    protected File configFile;
    protected JSONObject jsonConfig;

    private boolean fileChanged = false;

    public JSONConfiguration(File file) {
        this.configFile = file;
    }


    public Object get(String key) {
        return jsonConfig.get(key);
    }

    //Getters Start
    public Object getConfigOption(String key) {
        return this.jsonConfig.get(key);
    }

    public Object getConfigOption(String key, Object obj) {
        if (getConfigOption(key) == null) {
            return obj;
        }
        return getConfigOption(key);
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

    @Override
    public void writeConfigurationFile() {
        if (fileChanged) {
            saveFile();
        }
    }

    //Getters End

    public boolean containsKey(String key) {
        return jsonConfig.containsKey(key);
    }

    public void generateConfigOption(String key, Object value) {
        if (!jsonConfig.containsKey(key)) {
            jsonConfig.put(key, value);
            fileChanged = true;
        }
    }

    public void writeConfigOption(String key, Object value) {
        jsonConfig.put(key, value);
        this.fileChanged = true;
    }

    @Override
    public void load() {


        //Create directory
        if (!this.configFile.exists()) {
            this.configFile.getParentFile().mkdirs();
            jsonConfig = new JSONObject();
            saveFile();
        } else {
            try {
                JSONParser parser = new JSONParser();
                jsonConfig = (JSONObject) parser.parse(new FileReader(configFile));
            } catch (ParseException e) {
                System.out.println("Failed to load config file.");
                throw new RuntimeException(e + ": " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e + ": " + e.getMessage());
            }
        }
    }


    protected void saveFile() {
        try (FileWriter file = new FileWriter(configFile)) {
            file.write(jsonConfig.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
