package com.iafenvoy.jupiter._loader.fabric.network;

import com.iafenvoy.jupiter.network.ClientNetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class FabricClientNetworkHelper implements ClientNetworkHelper {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> id, ClientNetworkHelper.Handler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(id, (payload, ctx) -> {
            Runnable runnable = handler.handle(ctx.client(), payload);
            if (runnable != null) ctx.client().execute(runnable);
        });
    }
}
