package com.chaosgaming.velocityuuidforwarder;

import com.chaosgaming.velocityuuidforwarder.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VelocityUUIDForwarder implements ModInitializer {
    public static final String MOD_ID = "velocityuuidforwarder";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModConfig config;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Velocity UUID Forwarder mod...");
        
        // Load configuration
        config = ModConfig.load();
        
        // Validate configuration
        if ("CHANGE_ME".equals(config.getSecret())) {
            LOGGER.error("!!! WARNING: Velocity forwarding secret is set to default value!");
            LOGGER.error("!!! Please change the secret in config/velocity-uuid-forwarder.toml");
            LOGGER.error("!!! Connections will be rejected until the secret is properly configured.");
        } else {
            LOGGER.info("Velocity UUID Forwarder initialized successfully!");
            LOGGER.info("Debug mode: {}", config.isDebug());
        }
    }
    
    public static ModConfig getConfig() {
        return config;
    }
}
