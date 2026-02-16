package de.craftingworld.velocityuuidsync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "velocity-uuid-sync",
        name = "Velocity UUID Sync",
        version = "1.0.0",
        description = "Synchronizes UUIDs between online-mode and offline-mode servers",
        authors = {"Crafting-World"}
)
public class VelocityUuidSync {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private UuidStorageManager storageManager;
    private ConfigManager configManager;

    @Inject
    public VelocityUuidSync(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Velocity UUID Sync plugin...");
        
        try {
            // Initialize configuration
            configManager = new ConfigManager(dataDirectory);
            configManager.loadConfig();
            
            // Initialize storage manager
            storageManager = new UuidStorageManager(dataDirectory, logger);
            storageManager.initialize();
            
            logger.info("Velocity UUID Sync plugin initialized successfully!");
            logger.info("Monitoring offline-mode servers: " + configManager.getOfflineModeServers());
        } catch (Exception e) {
            logger.error("Failed to initialize Velocity UUID Sync plugin", e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Shutting down Velocity UUID Sync plugin...");
        if (storageManager != null) {
            storageManager.close();
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        // Store the player's authentic Mojang UUID when they login
        String username = event.getPlayer().getUsername();
        String uuid = event.getPlayer().getUniqueId().toString();
        
        try {
            storageManager.storeUuidMapping(username, uuid);
            if (configManager.isDebugEnabled()) {
                logger.info("Stored UUID mapping: {} -> {}", username, uuid);
            }
        } catch (Exception e) {
            logger.error("Failed to store UUID mapping for player " + username, e);
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // Check if the player is connecting to an offline-mode server
        // Use getOriginalServer() which is compatible with Velocity 3.4.0
        RegisteredServer originalServer = event.getOriginalServer();
        if (originalServer == null) {
            logger.warn("ServerPreConnectEvent fired with null originalServer for player {}", 
                    event.getPlayer().getUsername());
            return; // No server to connect to, skip processing
        }
        
        String targetServerName = originalServer.getServerInfo().getName();
        
        if (configManager.isOfflineModeServer(targetServerName)) {
            String username = event.getPlayer().getUsername();
            if (configManager.isDebugEnabled()) {
                logger.info("Player {} is connecting to offline-mode server: {}", username, targetServerName);
                logger.info("Player UUID: {}", event.getPlayer().getUniqueId());
            }
        }
    }

    @Subscribe
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        // This event allows us to modify the game profile before it's sent to the backend server
        String username = event.getUsername();
        
        try {
            String storedUuid = storageManager.getUuid(username);
            if (storedUuid != null && configManager.isDebugEnabled()) {
                logger.info("Game profile request for {}: stored UUID is {}", username, storedUuid);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve UUID for player " + username, e);
        }
    }
}
