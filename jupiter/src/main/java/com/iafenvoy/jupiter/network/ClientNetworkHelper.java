package com.iafenvoy.jupiter.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ClientNetworkHelper {
    void sendToServer(CustomPacketPayload payload);

    <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> id, Handler<T> handler);

    interface Handler<T extends CustomPacketPayload> {
        Runnable handle(Minecraft client, T payload);
    }
}