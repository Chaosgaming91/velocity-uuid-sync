package com.velocitypowered.api.event.player;

public class GameProfileRequestEvent {
    private String username;

    public GameProfileRequestEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
