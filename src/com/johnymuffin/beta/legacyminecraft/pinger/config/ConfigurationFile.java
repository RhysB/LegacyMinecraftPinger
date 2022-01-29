package com.johnymuffin.beta.legacyminecraft.pinger.config;

public interface ConfigurationFile {

    public Object getConfigOption(String key);

    public Object getConfigOption(String key, Object obj);

    public String getConfigString(String key);

    public Integer getConfigInteger(String key);

    public Long getConfigLong(String key);

    public Double getConfigDouble(String key);

    public Boolean getConfigBoolean(String key);

//    public void writeConfigurationFile();

    public void generateConfigOption(String key, Object defaultValue);

    public void load();

    public void writeConfigurationFile();

}
