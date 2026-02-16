package com.velocitypowered.api.event.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

public class ServerPreConnectEvent {
    private Player player;
    private Result result;

    public ServerPreConnectEvent(Player player, Result result) {
        this.player = player;
        this.result = result;
    }

    public Player getPlayer() {
        return player;
    }

    public Result getResult() {
        return result;
    }

    public static class Result {
        private RegisteredServer server;

        public Result(RegisteredServer server) {
            this.server = server;
        }

        public Optional<RegisteredServer> getServer() {
            return Optional.ofNullable(server);
        }
    }
}
