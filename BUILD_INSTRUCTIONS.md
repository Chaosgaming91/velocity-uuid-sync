# Build Instructions

This document provides detailed instructions for building the Velocity UUID Forwarder Fabric mod from source.

## Prerequisites

Before building, ensure you have the following installed:

### Required Software

1. **Java Development Kit (JDK) 21 or higher**
   ```bash
   # Check your Java version
   java -version
   
   # Should output something like:
   # openjdk version "21.0.x"
   ```

   Download from:
   - [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
   - [OpenJDK 21](https://adoptium.net/)

2. **Gradle 8.5 or higher** (wrapper included in repository)
   ```bash
   # The project includes a Gradle wrapper, so you don't need to install Gradle manually
   # But if you want to install it globally:
   # - Download from https://gradle.org/releases/
   ```

3. **Git** (for cloning the repository)
   ```bash
   git --version
   ```

### Network Requirements

The build process requires internet access to download:
- Minecraft server JAR
- Fabric Loader
- Fabric API
- Other dependencies (Netty, SLF4J, TOML4J, etc.)

Ensure you have access to:
- `https://maven.fabricmc.net/` (Fabric Maven repository)
- `https://repo.maven.apache.org/maven2/` (Maven Central)
- `https://launchermeta.mojang.com/` (Mojang metadata)
- `https://piston-data.mojang.com/` (Mojang artifacts)

## Building the Mod

### 1. Clone the Repository

```bash
git clone https://github.com/Chaosgaming91/velocity-uuid-sync.git
cd velocity-uuid-sync
```

### 2. Build with Gradle

The repository includes a Gradle wrapper, so you can build without installing Gradle:

#### On Linux/macOS:
```bash
./gradlew build
```

#### On Windows:
```cmd
gradlew.bat build
```

### 3. Build Output

After a successful build, you'll find the JAR files in:
```
build/libs/
├── velocityuuidforwarder-1.0.0.jar          # Main mod JAR
├── velocityuuidforwarder-1.0.0-sources.jar  # Source code JAR
```

The main JAR (`velocityuuidforwarder-1.0.0.jar`) is what you need to install on your server.

## Development Setup

### IntelliJ IDEA

1. **Import the project**
   ```bash
   # In the project directory
   ./gradlew genSources
   ./gradlew idea
   ```

2. **Open in IntelliJ**
   - File → Open
   - Select the project directory
   - IntelliJ will automatically import the Gradle project

3. **Run configuration**
   - Gradle will create run configurations automatically
   - Use `runServer` task to test the mod

### Eclipse

```bash
./gradlew genSources
./gradlew eclipse
```

Then import the project in Eclipse:
- File → Import → Existing Projects into Workspace

### Visual Studio Code

1. **Install Java Extension Pack**
   - Open VS Code
   - Install "Extension Pack for Java" from Microsoft

2. **Open the project**
   ```bash
   code .
   ```

3. **Generate sources**
   ```bash
   ./gradlew genSources
   ```

## Gradle Tasks

### Common Tasks

| Task | Description |
|------|-------------|
| `./gradlew build` | Build the mod JAR |
| `./gradlew clean` | Clean build artifacts |
| `./gradlew jar` | Create mod JAR without running tests |
| `./gradlew genSources` | Generate Minecraft source code for IDE |
| `./gradlew runServer` | Run a Minecraft server with the mod |
| `./gradlew idea` | Generate IntelliJ IDEA project files |
| `./gradlew eclipse` | Generate Eclipse project files |

### Advanced Tasks

| Task | Description |
|------|-------------|
| `./gradlew build --refresh-dependencies` | Force refresh all dependencies |
| `./gradlew build --stacktrace` | Build with full stack traces on error |
| `./gradlew build --info` | Build with detailed logging |
| `./gradlew tasks` | List all available Gradle tasks |

## Project Structure

```
velocity-uuid-sync/
├── build.gradle                     # Main build script
├── gradle.properties                # Project properties
├── settings.gradle                  # Gradle settings
├── src/
│   └── main/
│       ├── java/
│       │   └── com/chaosgaming/velocityuuidforwarder/
│       │       ├── VelocityUUIDForwarder.java        # Main mod class
│       │       ├── config/
│       │       │   └── ModConfig.java                # Configuration handler
│       │       ├── mixin/
│       │       │   └── ServerLoginNetworkHandlerMixin.java  # Login mixin
│       │       └── util/
│       │           └── VelocityMessageUtil.java      # Protocol handler
│       └── resources/
│           ├── fabric.mod.json                        # Mod metadata
│           ├── velocityuuidforwarder.mixins.json     # Mixin configuration
│           └── assets/velocityuuidforwarder/
│               └── icon.png                           # Mod icon (optional)
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew                          # Gradle wrapper script (Unix)
├── gradlew.bat                      # Gradle wrapper script (Windows)
└── FABRIC_README.md                 # User documentation
```

## Dependencies

The mod has the following dependencies (automatically downloaded by Gradle):

### Runtime Dependencies
- **Minecraft**: 1.21.1
- **Fabric Loader**: 0.16.10+
- **Fabric API**: 0.110.0+1.21.1
- **TOML4J**: 0.7.2 (bundled in JAR)

### Compile-time Only
- **Fabric Loom**: 1.14-SNAPSHOT (Gradle plugin)
- **Mojang Mappings**: Official mappings

## Troubleshooting

### Build Fails: "Could not resolve dependencies"

**Problem**: Gradle can't download dependencies

**Solution**:
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches

# Retry build
./gradlew build --refresh-dependencies
```

### Build Fails: "Java version mismatch"

**Problem**: Wrong Java version

**Solution**:
```bash
# Check Java version
java -version

# If not Java 21, install JDK 21 and set JAVA_HOME
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH

# Or use Gradle's Java toolchain (edit gradle.properties)
# Add: org.gradle.java.home=/path/to/jdk-21
```

### Build Fails: "Could not find fabric-loom"

**Problem**: Can't access Fabric Maven repository

**Solution**:
1. Check your internet connection
2. Verify you can access https://maven.fabricmc.net/
3. Check firewall/proxy settings
4. Try with VPN if network blocks Maven repositories

### Slow Build

**Problem**: First build takes a long time

**Solution**:
- This is normal! First build downloads Minecraft, all dependencies, and remaps code
- Subsequent builds will be much faster (seconds instead of minutes)
- Increase Gradle memory: Edit `gradle.properties`:
  ```properties
  org.gradle.jvmargs=-Xmx4G
  ```

### Out of Memory Error

**Problem**: Gradle runs out of memory during build

**Solution**:
Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4G
org.gradle.daemon=true
org.gradle.parallel=true
```

## Building for Distribution

### Create Release JAR

```bash
# Clean and build
./gradlew clean build

# The JAR is in build/libs/
cp build/libs/velocityuuidforwarder-1.0.0.jar ./velocityuuidforwarder-1.0.0.jar
```

### Verify JAR Contents

```bash
# List contents
jar tf build/libs/velocityuuidforwarder-1.0.0.jar

# Should include:
# - com/chaosgaming/velocityuuidforwarder/ (compiled classes)
# - fabric.mod.json
# - velocityuuidforwarder.mixins.json
# - com/moandjiezana/toml/ (bundled TOML4J)
```

### Test the Built JAR

1. **Set up test server**
   ```bash
   mkdir test-server
   cd test-server
   
   # Download Fabric server
   curl -OJ https://meta.fabricmc.net/v2/versions/loader/1.21.1/0.16.10/1.0.1/server/jar
   
   # Create mods directory
   mkdir mods
   
   # Copy your mod
   cp ../build/libs/velocityuuidforwarder-1.0.0.jar mods/
   
   # Download Fabric API
   # (get from https://modrinth.com/mod/fabric-api/versions)
   ```

2. **Run server**
   ```bash
   java -jar fabric-server-launch.jar nogui
   ```

3. **Check logs**
   - Look for: "Initializing Velocity UUID Forwarder mod..."
   - Verify configuration is created

## Continuous Integration

### GitHub Actions (Optional)

Create `.github/workflows/build.yml`:

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Upload artifact
      uses: actions/upload-artifact@v3
      with:
        name: velocityuuidforwarder
        path: build/libs/*.jar
```

## Version Management

To change the mod version:

1. **Edit `gradle.properties`**
   ```properties
   mod_version=1.1.0
   ```

2. **Rebuild**
   ```bash
   ./gradlew clean build
   ```

3. **New JAR will be**
   ```
   build/libs/velocityuuidforwarder-1.1.0.jar
   ```

## Additional Resources

- **Fabric Wiki**: https://fabricmc.net/wiki/
- **Fabric API Javadoc**: https://maven.fabricmc.net/docs/
- **Minecraft Wiki**: https://minecraft.wiki/
- **Yarn Mappings**: https://github.com/FabricMC/yarn

## Support

For build issues:
- Check existing GitHub issues: https://github.com/Chaosgaming91/velocity-uuid-sync/issues
- Create a new issue with:
  - Your OS and Java version
  - Full build log
  - Steps to reproduce
