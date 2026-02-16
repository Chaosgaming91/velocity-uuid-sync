package de.craftingworld.velocityuuidsync;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.sql.*;

public class UuidStorageManager {

    private final Path dataDirectory;
    private final Logger logger;
    private Connection connection;

    public UuidStorageManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void initialize() throws SQLException {
        // Explicitly load SQLite JDBC driver for plugin classloader compatibility
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found in classpath", e);
        }
        
        Path dbPath = dataDirectory.resolve("uuid-mappings.db");
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toString();
        
        connection = DriverManager.getConnection(jdbcUrl);
        createTables();
        
        logger.info("UUID storage initialized at: {}", dbPath);
    }

    private void createTables() throws SQLException {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS uuid_mappings (
                    username TEXT PRIMARY KEY,
                    uuid TEXT NOT NULL,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSql);
        }
    }

    public void storeUuidMapping(String username, String uuid) throws SQLException {
        String sql = """
                INSERT INTO uuid_mappings (username, uuid, last_updated)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(username) DO UPDATE SET
                    uuid = excluded.uuid,
                    last_updated = CURRENT_TIMESTAMP
                """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }

    public String getUuid(String username) throws SQLException {
        String sql = "SELECT uuid FROM uuid_mappings WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("uuid");
                }
            }
        }
        return null;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("UUID storage closed successfully");
            } catch (SQLException e) {
                logger.error("Failed to close database connection", e);
            }
        }
    }
}
