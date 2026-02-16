package de.craftingworld.velocityuuidsync;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigManager {

    private final Path dataDirectory;
    private final Path configFile;
    private Properties properties;
    private List<String> offlineModeServers;
    private boolean debugEnabled;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.properties");
        this.properties = new Properties();
        this.offlineModeServers = new ArrayList<>();
    }

    public void loadConfig() throws IOException {
        // Create data directory if it doesn't exist
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        // Create default config if it doesn't exist
        if (!Files.exists(configFile)) {
            createDefaultConfig();
        }

        // Load config
        try (InputStream input = Files.newInputStream(configFile)) {
            properties.load(input);
        }

        // Parse settings
        String serversConfig = properties.getProperty("offline-mode-servers", "create");
        for (String server : serversConfig.split(",")) {
            String trimmed = server.trim();
            if (!trimmed.isEmpty()) {
                offlineModeServers.add(trimmed.toLowerCase());
            }
        }

        debugEnabled = Boolean.parseBoolean(properties.getProperty("debug", "false"));
    }

    private void createDefaultConfig() throws IOException {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("offline-mode-servers", "create");
        defaultProps.setProperty("debug", "false");

        try (OutputStream output = Files.newOutputStream(configFile)) {
            defaultProps.store(output, "Velocity UUID Sync Configuration\n" +
                    "offline-mode-servers: Comma-separated list of server names that run in offline-mode\n" +
                    "debug: Enable debug logging (true/false)");
        }
    }

    public List<String> getOfflineModeServers() {
        return new ArrayList<>(offlineModeServers);
    }

    public boolean isOfflineModeServer(String serverName) {
        return offlineModeServers.contains(serverName.toLowerCase());
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
