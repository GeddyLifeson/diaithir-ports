package com.iafenvoy.jupiter.network;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.JupiterProxies;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.network.payload.ConfigErrorPayload;
import com.iafenvoy.jupiter.network.payload.ConfigRequestPayload;
import com.iafenvoy.jupiter.network.payload.ConfigSyncPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ServerConfigNetwork {
    public static void init() {
        JupiterProxies.SERVER_NETWORKING.registerReceiver(ConfigRequestPayload.TYPE, (server, player, payload) -> onConfigRequest(server, player, payload.id()));
        JupiterProxies.SERVER_NETWORKING.registerReceiver(ConfigSyncPayload.TYPE, (server, player, payload) -> onConfigSync(server, player, payload.id(), payload.compound()));
    }

    //Will only return config data if player has permission || allow manually sync to client.
    private static Runnable onConfigRequest(MinecraftServer server, ServerPlayer player, Identifier id) {
        Jupiter.LOGGER.info("Player {} request to get config {}", player.getName().getString(), id);
        boolean b = ServerConfigManager.checkPermission(id, server, player, false);
        CompoundTag compound;
        if (b) {
            AbstractConfigContainer data = ServerConfigManager.getConfig(id);
            if (data == null) {
                compound = new CompoundTag();
                b = false;
            } else compound = data.serializeNbt();
        } else compound = new CompoundTag();
        boolean finalB = b;
        return () -> JupiterProxies.SERVER_NETWORKING.sendToPlayer(player, new ConfigSyncPayload(id, finalB, compound));
    }

    private static Runnable onConfigSync(MinecraftServer server, ServerPlayer player, Identifier id, CompoundTag data) {
        Jupiter.LOGGER.info("Player {} request to change config {}", player.getName().getString(), id);
        return () -> {
            if (ServerConfigManager.checkPermission(id, server, player, true)) {
                AbstractConfigContainer container = ServerConfigManager.getConfig(id);
                if (container != null) {
                    Jupiter.LOGGER.info(data.toString());
                    container.deserializeNbt(data);
                    container.onConfigsChanged();
                    Jupiter.LOGGER.info("Player {} changed config {}", player.getName().getString(), id);
                }
            } else
                JupiterProxies.SERVER_NETWORKING.sendToPlayer(player, new ConfigErrorPayload());
        };
    }
}
