package com.velocitypowered.api.util;

import java.util.UUID;

/**
 * Represents a game profile with player information and UUID.
 * Used for UUID synchronization functionality.
 */
public class GameProfile {
    private final UUID id;
    private final String name;

    public GameProfile(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GameProfile withId(UUID id) {
        return new GameProfile(id, this.name);
    }
}
