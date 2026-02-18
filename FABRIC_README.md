# Velocity UUID Forwarder - Fabric Mod

A Fabric mod for Minecraft 1.21.1 servers that handles Velocity's modern player info forwarding protocol to ensure players get their correct Mojang UUIDs.

## Overview

This mod is designed to run on **Minecraft servers** (not the proxy) that are behind a Velocity proxy. It intercepts the login process to read and verify Velocity's forwarding data, ensuring that players maintain their authentic Mojang UUIDs instead of getting offline-mode UUIDs.

## Requirements

- **Minecraft Server**: 1.21.1
- **Fabric Loader**: 0.16.10 or higher
- **Fabric API**: 0.110.0+1.21.1 or higher
- **Java**: 21 or higher
- **Velocity Proxy**: Must be configured with `player-info-forwarding-mode = "modern"`

## Installation

### 1. Server Setup

1. **Install Fabric on your Minecraft server**
   ```bash
   # Download Fabric installer from https://fabricmc.net/use/
   java -jar fabric-installer.jar server -mcversion 1.21.1
   ```

2. **Install Fabric API**
   - Download Fabric API from [Modrinth](https://modrinth.com/mod/fabric-api) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
   - Place `fabric-api-x.x.x+1.21.1.jar` in the `mods/` folder

3. **Install Velocity UUID Forwarder**
   - Download `velocityuuidforwarder-1.0.0.jar` from releases
   - Place it in the `mods/` folder
   ```bash
   cp velocityuuidforwarder-1.0.0.jar /path/to/server/mods/
   ```

4. **Configure server.properties**
   ```properties
   # Set online-mode to false (Velocity handles authentication)
   online-mode=false
   ```

### 2. Configuration

On first run, the mod creates a config file at `config/velocity-uuid-forwarder.toml`:

```toml
# Velocity forwarding secret (must match velocity.toml)
secret = "CHANGE_ME"

# Enable debug logging
debug = false
```

**Important**: You **must** change the `secret` value to match your Velocity proxy configuration!

#### Getting the Secret from Velocity

The secret must match the one in your Velocity proxy's `velocity.toml`:

```toml
[forwarding]
mode = "modern"  # Must be "modern", not "legacy" or "none"
secret = "YOUR_SECRET_HERE"  # Copy this value to the Fabric mod config
```

**Example Configuration:**

If your `velocity.toml` has:
```toml
[forwarding]
mode = "modern"
secret = "EgAbmtTT4qGk"
```

Then your `config/velocity-uuid-forwarder.toml` should have:
```toml
secret = "EgAbmtTT4qGk"
debug = false
```

### 3. Restart Server

After configuring, restart your Minecraft server:
```bash
# Stop the server
# Start the server
```

## Verification

### Check Mod Loading

Look for this in the server console:
```
[INFO] Initializing Velocity UUID Forwarder mod...
[INFO] Velocity UUID Forwarder initialized successfully!
[INFO] Debug mode: false
```

### Verify UUID Forwarding

When a player connects, you should see their **Mojang UUID** (type 4) instead of an offline-mode UUID (type 3).

**Correct (Mojang UUID - type 4):**
```
UUID of player Steve is 069a79f4-44e9-4726-a5be-fca90e38aaf5
```

**Incorrect (Offline UUID - type 3):**
```
UUID of player Steve is 8667ba71-b85a-4004-af54-457a9734eed7
```

You can verify in-game with:
```
/data get entity @p UUID
```

The UUID should be identical across all servers in your network!

## Troubleshooting

### Issue: "Velocity forwarding secret is set to default value!"

**Symptom:** Error in console about secret being "CHANGE_ME"

**Solution:**
1. Edit `config/velocity-uuid-forwarder.toml`
2. Change `secret = "CHANGE_ME"` to your actual Velocity secret
3. Restart the server

### Issue: "Invalid forwarding signature"

**Symptom:** Players can't join, error about invalid signature

**Solution:**
- The secret in `velocity-uuid-forwarder.toml` doesn't match `velocity.toml`
- Copy the exact secret from your Velocity configuration
- Make sure there are no extra spaces or quotes
- Restart both Velocity and the Minecraft server

### Issue: Players still get offline UUIDs

**Symptom:** UUIDs are type 3 instead of type 4

**Solution:**
1. Verify Velocity is using `mode = "modern"` (not "legacy" or "none")
2. Check that `server.properties` has `online-mode=false`
3. Ensure the secret matches exactly
4. Enable debug mode to see what's happening:
   ```toml
   debug = true
   ```
5. Check server logs for detailed forwarding information

### Issue: Mod doesn't load

**Symptom:** No mod initialization messages in console

**Solution:**
- Verify you're running Fabric Loader 0.16.10+
- Ensure Java 21 is being used
- Check that Fabric API is installed
- Verify the mod JAR is in the `mods/` folder

### Debug Mode

Enable debug logging to troubleshoot issues:

```toml
secret = "YOUR_SECRET"
debug = true
```

With debug enabled, you'll see:
```
[INFO] Applied Velocity forwarding for player Steve with UUID 069a79f4-44e9-4726-a5be-fca90e38aaf5
[INFO] Original address: 192.168.1.100
```

## Security

This mod implements several security features:

✅ **HMAC-SHA256 Signature Verification** - All forwarding data is cryptographically verified
✅ **Secret Validation** - Warns if using default secret
✅ **Connection Rejection** - Invalid signatures are rejected immediately
✅ **Modern Protocol Only** - Only supports Velocity's secure modern forwarding

**Important:** Never share your forwarding secret publicly! It should be kept private between your Velocity proxy and Minecraft servers.

## How It Works

### Technical Overview

1. **Player Login Attempt**
   - Player connects through Velocity proxy
   - Velocity authenticates with Mojang and gets the player's UUID

2. **Forwarding Data**
   - Velocity sends forwarding data via plugin message channel
   - Data includes: UUID, username, IP address, game profile properties
   - Data is signed with HMAC-SHA256

3. **Mixin Interception**
   - Mod's mixin intercepts `ServerLoginNetworkHandler.onQueryResponse`
   - Reads the forwarding packet data

4. **Verification**
   - Verifies HMAC signature using configured secret
   - Rejects connection if signature is invalid

5. **UUID Application**
   - Extracts Mojang UUID from verified data
   - Replaces the GameProfile with correct UUID
   - Player joins with authentic Mojang UUID

### Velocity Modern Forwarding Protocol

The forwarding data format:
```
1. HMAC-SHA256 signature (32 bytes)
2. Version byte (1 for modern)
3. Player's real IP address (VarInt length + UTF-8)
4. Player's Mojang UUID (16 bytes)
5. Player's username (VarInt length + UTF-8)
6. Game profile properties (VarInt count + properties)
```

## Building from Source

### Prerequisites
- Java 21 JDK
- Gradle 8.5+ (wrapper included)
- Internet connection for dependencies

### Build Steps

```bash
# Clone the repository
git clone https://github.com/Chaosgaming91/velocity-uuid-sync.git
cd velocity-uuid-sync

# Build the mod
./gradlew build

# Output JAR will be in build/libs/
ls -l build/libs/velocityuuidforwarder-1.0.0.jar
```

### Development Setup

```bash
# Generate IDE files (IntelliJ IDEA)
./gradlew idea

# Generate IDE files (Eclipse)
./gradlew eclipse

# Run Minecraft with the mod for testing
./gradlew runServer
```

## Compatibility

### Tested With
- **Minecraft**: 1.21.1
- **Fabric Loader**: 0.16.10
- **Fabric API**: 0.110.0+1.21.1
- **Velocity**: 3.1.0+

### Server Software
- ✅ Vanilla Minecraft Server (with Fabric)
- ✅ Fabric Server
- ❌ Paper/Spigot (use Paper's built-in Velocity support)
- ❌ Forge (different mod loader)

## FAQ

**Q: Do I need this on my Velocity proxy?**
A: No! This is a **server-side mod** for Minecraft servers. Velocity already has forwarding built-in.

**Q: Will this work with Paper/Spigot?**
A: No, Paper has built-in Velocity support. This is specifically for Fabric servers.

**Q: Can I use legacy BungeeCord forwarding?**
A: No, this mod only supports Velocity's modern forwarding protocol for security reasons.

**Q: Do players need to install this mod?**
A: No, this is a server-side mod. Players don't need anything special.

**Q: Will this work with other mods?**
A: Yes, it should be compatible with most Fabric mods since it only affects the login process.

**Q: What about player skins?**
A: Player skins, capes, and other profile properties are forwarded along with the UUID.

## Support

For issues, questions, or contributions:
- **GitHub Issues**: https://github.com/Chaosgaming91/velocity-uuid-sync/issues
- **Server**: crafting-world.de

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

- **Author**: Crafting-World Team
- **Maintainer**: Chaosgaming91
- **Based on**: Velocity's modern forwarding protocol specification

## Changelog

### Version 1.0.0 (2026-02-18)
- Initial release
- Velocity modern forwarding support
- HMAC-SHA256 signature verification
- Automatic UUID forwarding
- TOML configuration
- Debug logging support
