# Installation Guide

This guide walks you through installing and configuring the Velocity UUID Sync plugin on your server.

## Step 1: Verify Prerequisites

Before installing, make sure you have:

1. **Velocity Proxy** (version 3.1.0 or higher)
   ```bash
   # Check your Velocity version in the console when starting
   java -jar velocity.jar
   ```

2. **Java 17 or higher**
   ```bash
   java -version
   # Should show: java version "17" or higher
   ```

3. **Proper Velocity Configuration**
   Edit `velocity.toml` and ensure:
   ```toml
   online-mode = true
   player-info-forwarding-mode = "modern"
   ```

## Step 2: Download the Plugin

### Option A: Download Pre-built JAR
1. Go to [Releases](https://github.com/Chaosgaming91/velocity-uuid-sync/releases)
2. Download `velocity-uuid-sync-1.0.0.jar`

### Option B: Build from Source
```bash
git clone https://github.com/Chaosgaming91/velocity-uuid-sync.git
cd velocity-uuid-sync
mvn clean package
# JAR will be in target/velocity-uuid-sync-1.0.0.jar
```

## Step 3: Install the Plugin

1. **Copy to Velocity plugins folder**
   ```bash
   # Linux/macOS
   cp velocity-uuid-sync-1.0.0.jar /path/to/velocity/plugins/
   
   # Windows
   copy velocity-uuid-sync-1.0.0.jar C:\velocity\plugins\
   ```

2. **Verify file permissions** (Linux/macOS only)
   ```bash
   chmod 644 /path/to/velocity/plugins/velocity-uuid-sync-1.0.0.jar
   ```

## Step 4: Configure Backend Servers

### For Paper Servers (Lobby, Shop, Survival)

Edit `config/paper-global.yml`:
```yaml
proxies:
  velocity:
    enabled: true
    online-mode: true
    secret: 'YOUR_SECRET_FROM_VELOCITY_forwarding.secret'
```

### For Fabric Server with Cardboard (Create)

1. **Install Cardboard** (if not already installed)
   - Download from https://github.com/CardboardPowered/cardboard
   - Place in `mods/` folder

2. **Configure server.properties**
   ```properties
   online-mode=false
   server-port=25569
   ```

3. **Install FabricProxy-Lite** (required for Velocity forwarding)
   - Download from https://www.curseforge.com/minecraft/mc-mods/fabricproxy-lite
   - Place in `mods/` folder
   - Edit `config/FabricProxy-Lite.toml`:
   ```toml
   [proxy]
   enable = true
   mode = "velocity"
   secret = "YOUR_SECRET_FROM_VELOCITY_forwarding.secret"
   ```

## Step 5: Start Velocity

```bash
# Stop Velocity if running
# Linux/macOS
./velocity.sh stop

# Windows
velocity.bat stop

# Start Velocity
# Linux/macOS
./velocity.sh start

# Windows
velocity.bat start
```

## Step 6: Verify Installation

1. **Check Velocity console** for:
   ```
   [velocity-uuid-sync] Initializing Velocity UUID Sync plugin...
   [velocity-uuid-sync] UUID storage initialized at: plugins/velocity-uuid-sync/uuid-mappings.db
   [velocity-uuid-sync] Velocity UUID Sync plugin initialized successfully!
   [velocity-uuid-sync] Monitoring offline-mode servers: [create]
   ```

2. **Check plugin files created**:
   ```bash
   ls -la plugins/velocity-uuid-sync/
   # Should show:
   # - config.properties
   # - uuid-mappings.db
   ```

## Step 7: Configure the Plugin

Edit `plugins/velocity-uuid-sync/config.properties`:

```properties
# List your offline-mode server names (from velocity.toml [servers] section)
offline-mode-servers=create

# Enable debug logging during initial testing
debug=true
```

**Note**: After initial testing, set `debug=false` to reduce log spam.

## Step 8: Test the Setup

1. **Connect to Velocity**
   - Open Minecraft
   - Connect to your Velocity proxy address (e.g., `play.crafting-world.de`)

2. **Check UUID on Paper server**
   ```
   /server lobby
   /minecraft:data get entity @p UUID
   # Note down the UUID shown
   ```

3. **Check UUID on Fabric server**
   ```
   /server create
   /minecraft:data get entity @p UUID
   # Should show THE SAME UUID!
   ```

4. **Test Economy**
   ```
   # On Lobby
   /balance
   # Remember your balance
   
   # On Create
   /balance
   # Should show same balance
   ```

5. **Test Permissions**
   ```
   # On Lobby
   /lp user <yourname> info
   
   # On Create
   /lp user <yourname> info
   # Should show same permissions
   ```

## Step 9: Migrate Existing Players (Optional)

If you have existing players with different UUIDs in your database, you'll need to migrate:

### For LuckPerms
```bash
# On each server, identify old offline-mode UUIDs
# Then migrate permissions:
/lp bulkupdate users delete permission "<permission>" "<offline-uuid>"
/lp bulkupdate users set permission "<permission>" "<online-uuid>"
```

### For Economy Plugins
Consult your economy plugin documentation for UUID migration tools.

## Troubleshooting

### Plugin doesn't load
- Check Java version: `java -version` (must be 17+)
- Check Velocity version in console (must be 3.1.0+)
- Look for errors in `logs/latest.log`

### UUIDs still different
- Verify `velocity.toml` settings
- Verify backend server forwarding configuration
- Check `forwarding.secret` matches on all servers
- Enable debug mode and watch console logs

### Economy/Permissions don't work
- This is likely old data with offline UUIDs
- You need to migrate existing player data
- Check with your plugin documentation

### "Connection failed" on backend servers
- Verify `forwarding.secret` matches everywhere
- Check firewall rules
- Ensure backend servers accept Velocity forwarding

## Advanced Configuration

### Multiple Offline-Mode Servers

If you have multiple offline-mode servers:

```properties
offline-mode-servers=create,minigames,creative,event
```

### Debug Mode

Enable detailed logging:

```properties
debug=true
```

You'll see logs like:
```
[velocity-uuid-sync] Stored UUID mapping: Steve -> 12345678-1234-1234-1234-123456789abc
[velocity-uuid-sync] Player Steve is connecting to offline-mode server: create
```

## File Locations

```
velocity/
├── velocity.toml                          # Main Velocity config
├── forwarding.secret                      # Secret key for server forwarding
├── plugins/
│   ├── velocity-uuid-sync-1.0.0.jar      # Plugin JAR
│   └── velocity-uuid-sync/                # Plugin data directory
│       ├── config.properties              # Plugin configuration
│       └── uuid-mappings.db               # SQLite database
└── logs/
    └── latest.log                         # Velocity logs
```

## Getting Help

If you encounter issues:

1. **Enable debug mode** in `config.properties`
2. **Check logs** in `velocity/logs/latest.log`
3. **Open an issue** on GitHub with:
   - Your Velocity version
   - Your Minecraft version
   - Server setup (Paper/Fabric versions)
   - Error messages from logs
   - What you've already tried

## Next Steps

After successful installation:

1. Set `debug=false` in config
2. Test with all your players
3. Monitor logs for any issues
4. Backup your database regularly: `velocity/plugins/velocity-uuid-sync/uuid-mappings.db`
