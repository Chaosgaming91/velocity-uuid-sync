package com.velocitypowered.api.event.connection;

import com.velocitypowered.api.proxy.Player;

public class LoginEvent {
    private Player player;

    public LoginEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
