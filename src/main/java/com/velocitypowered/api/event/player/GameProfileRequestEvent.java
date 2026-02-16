package com.velocitypowered.api.event.player;

import com.velocitypowered.api.util.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class GameProfileRequestEvent {
    private String username;
    private GameProfile gameProfile;

    public GameProfileRequestEvent(String username) {
        this.username = username;
        // Initialize with a default offline-mode UUID (Type 3)
        this.gameProfile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)), username);
    }

    public String getUsername() {
        return username;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }
}
