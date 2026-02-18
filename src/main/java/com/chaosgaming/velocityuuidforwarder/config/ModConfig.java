package com.chaosgaming.velocityuuidforwarder.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    private static final String CONFIG_PATH = "config/velocity-uuid-forwarder.toml";
    
    private String secret;
    private boolean debug;
    
    public ModConfig() {
        this.secret = "CHANGE_ME";
        this.debug = false;
    }
    
    public static ModConfig load() {
        File configFile = new File(CONFIG_PATH);
        ModConfig config = new ModConfig();
        
        if (!configFile.exists()) {
            // Create default config
            config.save();
            return config;
        }
        
        try {
            Toml toml = new Toml().read(configFile);
            config.secret = toml.getString("secret", "CHANGE_ME");
            config.debug = toml.getBoolean("debug", false);
        } catch (Exception e) {
            System.err.println("Failed to load config, using defaults: " + e.getMessage());
        }
        
        return config;
    }
    
    public void save() {
        File configFile = new File(CONFIG_PATH);
        File parentDir = configFile.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("secret", secret);
        configMap.put("debug", debug);
        
        TomlWriter writer = new TomlWriter();
        try {
            writer.write(configMap, configFile);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
    
    public String getSecret() {
        return secret;
    }
    
    public boolean isDebug() {
        return debug;
    }
}
