# Velocity UUID Sync Plugin

A Velocity proxy plugin that synchronizes UUIDs between online-mode Paper servers and offline-mode Fabric servers (with Cardboard) in a Minecraft server network.

## Problem Statement

When running a Minecraft network with Velocity proxy where some servers run in online-mode (Paper) and others in offline-mode (Fabric with Cardboard), players get different UUIDs on different servers. This causes:
- ❌ Economy plugins (RedisEconomy) lose player balance
- ❌ Permission plugins (LuckPerms) lose permissions
- ❌ Data sync plugins (HuskSync) fail to sync
- ❌ Display plugins (TAB) show corrupted data
- ❌ Player skins don't load correctly

## Solution

This plugin ensures that all players have the same UUID across all servers by:
1. Capturing the authentic Mojang UUID when players connect through Velocity
2. Storing UUID mappings persistently in SQLite
3. Making these UUIDs available across all backend servers

## Requirements

- **Velocity Proxy**: Version 3.1.0 or higher
- **Minecraft Version**: 1.21.11 (compatible with other versions)
- **Java**: 17 or higher

## Installation

1. **Download the Plugin**
   - Download `velocity-uuid-sync-1.0.0.jar` from the releases page
   - Or build from source (see Building section below)

2. **Install on Velocity**
   ```bash
   # Copy the JAR to your Velocity plugins folder
   cp velocity-uuid-sync-1.0.0.jar /path/to/velocity/plugins/
   ```

3. **Configure Velocity**
   
   Your `velocity.toml` should already have these settings:
   ```toml
   online-mode = true
   player-info-forwarding-mode = "modern"
   forwarding-secret-file = "forwarding.secret"
   ```

4. **Restart Velocity**
   ```bash
   # Stop Velocity
   # Start Velocity
   ```

5. **Verify Installation**
   
   Check the Velocity console for:
   ```
   [INFO] Initializing Velocity UUID Sync plugin...
   [INFO] Velocity UUID Sync plugin initialized successfully!
   ```

## Configuration

On first run, the plugin creates a config file at `plugins/velocity-uuid-sync/config.properties`:

```properties
# Comma-separated list of server names that run in offline-mode
offline-mode-servers=create

# Enable debug logging (true/false)
debug=false
```

### Configuration Options

- **offline-mode-servers**: Comma-separated list of server names (as defined in `velocity.toml`) that run in offline-mode
  - Example: `create` (single server)
  - Example: `create,minigames,event` (multiple servers)

- **debug**: Enable verbose logging for troubleshooting
  - `false` (default): Normal logging
  - `true`: Detailed logging of all UUID operations

## How It Works

### Architecture

```
Player connects → Velocity Proxy → Authenticates with Mojang → Gets online-mode UUID
                       ↓
                 Stores UUID mapping (Username → UUID)
                       ↓
         Player connects to backend server (Paper or Fabric)
                       ↓
              UUID is forwarded consistently
```

### Technical Details

1. **Player Login**
   - When a player connects to Velocity, the plugin intercepts the `LoginEvent`
   - The authentic Mojang UUID is captured and stored in SQLite database
   - Mapping: `username → uuid` with timestamp

2. **UUID Forwarding**
   - Velocity's modern forwarding mode passes player information to backend servers
   - Both Paper and Fabric servers receive the same UUID from Velocity
   - The plugin monitors connections to ensure consistency

3. **Persistent Storage**
   - UUID mappings are stored in `plugins/velocity-uuid-sync/uuid-mappings.db`
   - SQLite database ensures data persists across restarts
   - Automatic table creation on first run

## Troubleshooting

### Verify Plugin is Working

1. **Check Plugin Load**
   ```bash
   # In Velocity console, check for:
   [INFO] Velocity UUID Sync plugin initialized successfully!
   ```

2. **Enable Debug Mode**
   - Edit `plugins/velocity-uuid-sync/config.properties`
   - Set `debug=true`
   - Restart Velocity
   - Connect with a player and check logs

3. **Check UUID Consistency**
   
   On each server (Paper and Fabric), run:
   ```
   /minecraft:data get entity @p UUID
   ```
   The UUID should be identical on all servers!

### Common Issues

#### Problem: Plugin doesn't load
**Solution**: 
- Ensure you're running Velocity 3.1.0+
- Check Velocity console for errors
- Verify Java 17+ is installed

#### Problem: Different UUIDs on different servers
**Solution**:
- Verify `velocity.toml` has `player-info-forwarding-mode = "modern"`
- Check that backend servers have player info forwarding enabled
- For Paper: `config/paper-global.yml` should have `velocity-support.enabled: true`
- For Fabric: Ensure Cardboard or FabricProxy-Lite is installed and configured

#### Problem: Economy/Permissions still broken
**Solution**:
- This might be a migration issue with existing data
- Backend plugins (LuckPerms, RedisEconomy) might have old data with offline UUIDs
- You may need to migrate old player data to use new UUIDs
- Contact support for specific plugin migration guides

### Logging

With `debug=true`, you'll see:
```
[INFO] Stored UUID mapping: PlayerName -> 12345678-1234-1234-1234-123456789abc
[INFO] Player PlayerName is connecting to offline-mode server: create
[INFO] Player UUID: 12345678-1234-1234-1234-123456789abc
```

## Testing

### Test Checklist

- [ ] Player can join Velocity proxy
- [ ] Player can connect to all servers (Lobby, Shop, Survival, Create)
- [ ] UUID is identical on all servers (use `/data get entity @p UUID`)
- [ ] RedisEconomy balance persists across servers
- [ ] LuckPerms permissions work on Fabric server
- [ ] HuskSync syncs inventory properly
- [ ] TAB displays correct information
- [ ] Player skin loads on all servers

### Manual Testing

1. **Connect to Velocity**
   ```
   minecraft -join your-server.com:25577
   ```

2. **Join Paper server (e.g., Lobby)**
   ```
   /server lobby
   /data get entity @p UUID
   # Note the UUID
   ```

3. **Join Fabric server (Create)**
   ```
   /server create
   /data get entity @p UUID
   # Compare with previous UUID - they should match!
   ```

4. **Test Economy**
   ```
   # On Lobby (Paper)
   /balance
   # Note your balance
   
   # On Create (Fabric)
   /balance
   # Should show same balance
   ```

## Building from Source

### Prerequisites
- Java 17 JDK
- Maven 3.6+

### Build Steps

```bash
# Clone the repository
git clone https://github.com/Chaosgaming91/velocity-uuid-sync.git
cd velocity-uuid-sync

# Build with Maven
mvn clean package

# Output JAR will be in target/
ls -l target/velocity-uuid-sync-1.0.0.jar
```

## Technical Specifications

### Dependencies
- **SQLite JDBC** (3.45.0.0) - Embedded database for UUID storage
- **Gson** (2.10.1) - JSON handling
- **SLF4J** (provided by Velocity) - Logging
- **Guice** (provided by Velocity) - Dependency injection

### Database Schema

```sql
CREATE TABLE uuid_mappings (
    username TEXT PRIMARY KEY,
    uuid TEXT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### File Structure
```
velocity/
└── plugins/
    ├── velocity-uuid-sync-1.0.0.jar
    └── velocity-uuid-sync/
        ├── config.properties
        └── uuid-mappings.db
```

## Compatibility

### Tested With
- **Velocity**: 3.1.0+
- **Paper**: 1.21.11
- **Fabric**: 1.21.11 with Cardboard
- **Plugins**: RedisEconomy, LuckPerms, HuskSync, HuskTowns, TAB, VotingPlugin

### Minecraft Versions
- Designed for 1.21.11
- Should work with other 1.20+ versions
- Not tested with older versions

## Support

For issues, questions, or contributions:
- **GitHub Issues**: https://github.com/Chaosgaming91/velocity-uuid-sync/issues
- **Server**: crafting-world.de

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

- **Author**: Crafting-World Team
- **Maintainer**: Chaosgaming91
- **Server**: crafting-world.de

## Changelog

### Version 1.0.0 (2026-02-16)
- Initial release
- UUID synchronization between online-mode and offline-mode servers
- SQLite-based persistent storage
- Configurable offline-mode server list
- Debug logging support
