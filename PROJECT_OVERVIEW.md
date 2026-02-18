# Velocity UUID Sync - Project Overview

This repository contains two complementary components for maintaining consistent player UUIDs across a Velocity-based Minecraft server network:

1. **Velocity Proxy Plugin** (Maven-based) - For the Velocity proxy server
2. **Fabric Server Mod** (Gradle-based) - For Fabric Minecraft servers

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        INTERNET                              │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
         ┌────────────────┐
         │  Velocity Proxy │ ← velocity-uuid-sync PLUGIN
         │  (Java 17+)     │    (stores UUID mappings)
         └────────┬───────┘
                  │
        ┌─────────┴──────────┐
        │                    │
        ▼                    ▼
┌──────────────┐    ┌──────────────┐
│ Paper Server │    │ Fabric Server│ ← velocityuuidforwarder MOD
│ (Online mode)│    │ (with Fabric)│    (handles forwarding)
│              │    │              │
│ Uses native  │    │ Uses mod to  │
│ Velocity     │    │ read Velocity│
│ forwarding   │    │ forwarding   │
└──────────────┘    └──────────────┘
```

## Component 1: Velocity Proxy Plugin

**Purpose**: Stores and manages UUID mappings at the proxy level

**Technology Stack**:
- Build System: Maven
- Java Version: 17+
- Dependencies: SQLite JDBC, Velocity API

**Key Features**:
- Captures Mojang UUIDs when players authenticate through Velocity
- Stores mappings in SQLite database
- Monitors player connections across servers
- Provides UUID lookup functionality

**Files**:
- `pom.xml` - Maven configuration
- `src/main/java/de/craftingworld/velocityuuidsync/` - Plugin source code
- `src/main/resources/velocity-plugin.json` - Plugin metadata

**Documentation**:
- `README.md` - Main project README (for the proxy plugin)
- `DEVELOPERS.md` - Developer documentation
- `INSTALL.md` - Installation guide
- `SUMMARY.md` - Project summary

## Component 2: Fabric Server Mod

**Purpose**: Handles Velocity's modern forwarding protocol on Fabric servers

**Technology Stack**:
- Build System: Gradle + Fabric Loom
- Java Version: 21
- Minecraft Version: 1.21.1
- Dependencies: Fabric Loader, Fabric API, TOML4J

**Key Features**:
- Intercepts server login process via Mixin
- Reads and verifies Velocity's forwarding data
- Validates HMAC-SHA256 signatures
- Applies correct Mojang UUIDs to players
- TOML-based configuration

**Files**:
- `build.gradle` - Gradle build configuration
- `gradle.properties` - Project properties
- `settings.gradle` - Gradle settings
- `src/main/java/com/chaosgaming/velocityuuidforwarder/` - Mod source code
- `src/main/resources/fabric.mod.json` - Fabric mod metadata
- `src/main/resources/velocityuuidforwarder.mixins.json` - Mixin configuration

**Documentation**:
- `FABRIC_README.md` - Fabric mod user guide
- `BUILD_INSTRUCTIONS.md` - Build instructions for developers
- `config-example/` - Example configuration files

## When to Use Each Component

### Use the Velocity Plugin when:
- ✅ You have a Velocity proxy
- ✅ You want centralized UUID management
- ✅ You need UUID persistence across restarts
- ✅ You're mixing Paper and Fabric servers

### Use the Fabric Mod when:
- ✅ You have Fabric servers behind Velocity
- ✅ You need Velocity's modern forwarding support
- ✅ You want players to get their Mojang UUIDs
- ❌ NOT for Paper servers (Paper has built-in Velocity support)
- ❌ NOT for standalone servers (requires Velocity proxy)

## Setup Scenarios

### Scenario 1: Paper + Fabric Network

**Setup**:
1. **On Velocity Proxy**: Install the velocity-uuid-sync plugin
2. **On Fabric Servers**: Install the velocityuuidforwarder mod
3. **On Paper Servers**: Use native Velocity forwarding (no mod needed)

**Configuration**:
- All servers must use `online-mode=false`
- Velocity must use `player-info-forwarding-mode = "modern"`
- All servers must have the same forwarding secret

### Scenario 2: Pure Fabric Network

**Setup**:
1. **On Velocity Proxy**: Configure modern forwarding (plugin optional)
2. **On All Fabric Servers**: Install the velocityuuidforwarder mod

**Configuration**:
- All servers: `online-mode=false`
- Velocity: `player-info-forwarding-mode = "modern"`
- All Fabric servers: Configure the same secret in `config/velocity-uuid-forwarder.toml`

### Scenario 3: Pure Paper Network

**Setup**:
1. **On Velocity Proxy**: Configure modern forwarding
2. **On All Paper Servers**: Configure Velocity support

**Configuration**:
- All servers: `online-mode=false`
- Velocity: `player-info-forwarding-mode = "modern"`
- Paper: `velocity-support.enabled: true` in `paper-global.yml`

## Configuration Sync

Both components require matching secrets:

**Velocity (`velocity.toml`)**:
```toml
[forwarding]
mode = "modern"
secret = "YOUR_SECRET_HERE"
```

**Fabric Mod (`config/velocity-uuid-forwarder.toml`)**:
```toml
secret = "YOUR_SECRET_HERE"
debug = false
```

**Paper (`config/paper-global.yml`)**:
```yaml
proxies:
  velocity:
    enabled: true
    online-mode: true
    secret: "YOUR_SECRET_HERE"
```

## Building

### Build the Velocity Plugin (Maven)

```bash
mvn clean package
# Output: target/velocity-uuid-sync-1.0.0.jar
```

### Build the Fabric Mod (Gradle)

```bash
./gradlew build
# Output: build/libs/velocityuuidforwarder-1.0.0.jar
```

## Development

### Project Structure

```
velocity-uuid-sync/
├── pom.xml                              # Maven config (Velocity plugin)
├── build.gradle                         # Gradle config (Fabric mod)
├── gradle.properties                    # Gradle properties (Fabric mod)
├── settings.gradle                      # Gradle settings (Fabric mod)
│
├── src/main/
│   ├── java/
│   │   ├── de/craftingworld/           # Velocity plugin code
│   │   └── com/chaosgaming/            # Fabric mod code
│   └── resources/
│       ├── velocity-plugin.json         # Velocity plugin metadata
│       ├── fabric.mod.json              # Fabric mod metadata
│       └── velocityuuidforwarder.mixins.json  # Mixin config
│
├── README.md                            # Velocity plugin documentation
├── FABRIC_README.md                     # Fabric mod documentation
├── BUILD_INSTRUCTIONS.md                # Build guide
├── DEVELOPERS.md                        # Developer guide
├── INSTALL.md                           # Installation guide
└── config-example/                      # Example configurations
```

### Adding Features

**To modify the Velocity plugin**:
1. Edit files in `src/main/java/de/craftingworld/`
2. Use Maven: `mvn clean package`
3. Test with a Velocity proxy

**To modify the Fabric mod**:
1. Edit files in `src/main/java/com/chaosgaming/`
2. Use Gradle: `./gradlew build`
3. Test with a Fabric server

## Testing

### Test the Velocity Plugin

1. Set up a Velocity proxy
2. Copy the plugin JAR to `plugins/`
3. Check logs for initialization
4. Connect with a test player
5. Verify UUID is stored in database

### Test the Fabric Mod

1. Set up a Fabric server with Fabric API
2. Copy the mod JAR to `mods/`
3. Configure the secret
4. Start server and check logs
5. Connect through Velocity
6. Verify UUID with `/data get entity @p UUID`

## Troubleshooting

### Different UUIDs on different servers

**Problem**: Players have different UUIDs on Fabric vs Paper servers

**Solution**:
1. Verify Velocity is using `mode = "modern"`
2. Check all secrets match exactly
3. Ensure Fabric servers have the mod installed
4. Restart all servers after configuration changes

### "Invalid signature" errors

**Problem**: Players can't join, signature verification fails

**Solution**:
1. Secrets don't match between Velocity and servers
2. Copy the exact secret from `velocity.toml`
3. No extra spaces or quotes
4. Restart everything

### Build failures

**Problem**: Maven or Gradle build fails

**Solution for Maven**:
```bash
mvn clean install -U
```

**Solution for Gradle**:
```bash
./gradlew clean build --refresh-dependencies
```

## Security

Both components implement Velocity's modern forwarding protocol with:

- ✅ HMAC-SHA256 signature verification
- ✅ Cryptographic secret validation
- ✅ Rejection of invalid forwarding data
- ✅ Protection against spoofed player data

**Security Best Practices**:
1. Use a strong, random secret
2. Keep secrets private (don't commit to repositories)
3. Use the same secret across all servers
4. Regularly update components for security patches
5. Monitor logs for signature verification failures

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## Support

- **GitHub Issues**: https://github.com/Chaosgaming91/velocity-uuid-sync/issues
- **Server**: crafting-world.de

## Credits

- **Author**: Crafting-World Team
- **Maintainer**: Chaosgaming91
- **Based on**: Velocity's modern forwarding protocol

## Version History

### 1.0.0 (2026-02-18)
- Initial release
- Velocity proxy plugin for UUID storage
- Fabric mod for modern forwarding support
- HMAC-SHA256 signature verification
- Comprehensive documentation
