# Notes for Developers

## Velocity API Stubs

This project includes minimal Velocity API stubs in `src/main/java/com/velocitypowered/` because the official Velocity API is not available in Maven Central and the PaperMC repository may be inaccessible in some build environments.

### Why Stubs?

1. The official Velocity API is hosted at `https://repo.papermc.io/repository/maven-public/`
2. This repository may be blocked or inaccessible in CI/CD environments
3. The stubs allow the plugin to compile without network dependencies
4. At runtime, Velocity provides the real API classes

### Important Notes

- **These stubs are only for compilation** - they are NOT included in the final JAR
- The actual Velocity API is provided by the Velocity proxy at runtime
- The stubs match the interface of Velocity API 3.1.0+
- When deployed to a real Velocity server, the plugin uses Velocity's actual API classes

### If You Want to Use Real API

To use the real Velocity API (if you have access to PaperMC repository):

1. Remove the stub files in `src/main/java/com/velocitypowered/`
2. Add this to `pom.xml`:
   ```xml
   <repositories>
       <repository>
           <id>papermc</id>
           <url>https://repo.papermc.io/repository/maven-public/</url>
       </repository>
   </repositories>
   
   <dependencies>
       <dependency>
           <groupId>com.velocitypowered</groupId>
           <artifactId>velocity-api</artifactId>
           <version>3.1.1</version>
           <scope>provided</scope>
       </dependency>
   </dependencies>
   ```

## Building

### Standard Build
```bash
mvn clean package
```

### Skip Tests (if any)
```bash
mvn clean package -DskipTests
```

### Build with Specific Java Version
```bash
export JAVA_HOME=/path/to/java17
mvn clean package
```

## Testing Locally

To test the plugin locally:

1. Set up a local Velocity server
2. Copy the built JAR to `velocity/plugins/`
3. Start Velocity
4. Connect with a Minecraft client
5. Check logs for proper initialization

## Project Structure

```
velocity-uuid-sync/
├── pom.xml                                    # Maven build configuration
├── README.md                                  # Main documentation
├── INSTALL.md                                 # Installation guide
├── LICENSE                                    # MIT License
├── DEVELOPERS.md                              # This file
└── src/
    └── main/
        └── java/
            ├── com/velocitypowered/api/       # Velocity API stubs (for compilation only)
            │   ├── event/                     # Event classes
            │   ├── plugin/                    # Plugin annotations
            │   └── proxy/                     # Proxy interfaces
            └── de/craftingworld/velocityuuidsync/
                ├── VelocityUuidSync.java      # Main plugin class
                ├── UuidStorageManager.java    # SQLite storage manager
                └── ConfigManager.java         # Configuration handler
```

## Contributing

When contributing:

1. **Don't modify the API stubs** unless absolutely necessary
2. **Keep dependencies minimal** - only add what's truly needed
3. **Test on a real Velocity server** - stubs are not a replacement for real testing
4. **Follow existing code style** - consistent formatting helps readability
5. **Update documentation** - if you change behavior, update the README

## Common Development Tasks

### Add a New Configuration Option

1. Edit `ConfigManager.java` to add the property
2. Update `createDefaultConfig()` to include it
3. Add getter method for the property
4. Update `README.md` to document it

### Add New Event Handling

1. Create the event handler method in `VelocityUuidSync.java`
2. Annotate with `@Subscribe`
3. Add any necessary API stubs if using new Velocity events
4. Test on a real Velocity server

### Change Database Schema

1. Edit `UuidStorageManager.java`
2. Update `createTables()` method
3. Consider migration path for existing databases
4. Test with both new and existing databases

## Known Limitations

1. **API Stubs are minimal** - only includes what's needed for this plugin
2. **No annotation processor** - Velocity's annotation processor is not included
3. **Runtime dependencies only** - compile-time checks are limited

## Deployment Checklist

Before releasing a new version:

- [ ] Bump version in `pom.xml`
- [ ] Update CHANGELOG in `README.md`
- [ ] Test on real Velocity server
- [ ] Test with Paper backend
- [ ] Test with Fabric backend
- [ ] Verify UUID consistency
- [ ] Check all plugin integrations work
- [ ] Build with `mvn clean package`
- [ ] Test the built JAR on a server
- [ ] Create GitHub release
- [ ] Tag the version in git

## Troubleshooting Build Issues

### "Cannot find symbol" errors
- Check that API stubs match what you're using
- Verify imports are correct
- Make sure you're not using Velocity API features that aren't stubbed

### Shade plugin warnings
- These are usually harmless
- They occur when dependencies have overlapping classes
- The Maven Shade plugin will handle them correctly

### Large JAR file size (14MB)
- This is normal - it includes SQLite native libraries
- SQLite includes native code for multiple platforms (Windows, Linux, macOS)
- This is necessary for cross-platform compatibility

## Support

For development questions:
- Check existing code and comments
- Review Velocity API documentation: https://docs.papermc.io/velocity
- Open an issue on GitHub
