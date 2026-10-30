package com.iafenvoy.jupiter._loader.fabric.network;

import com.iafenvoy.jupiter.network.ServerNetworkHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class FabricServerNetworkHelper implements ServerNetworkHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public <T extends CustomPacketPayload> void registerPayloadType(CustomPacketPayload.Type<T> id, StreamCodec<FriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundPlay().register(id, codec);
        PayloadTypeRegistry.serverboundPlay().register(id, codec);
    }

    @Override
    public <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> id, ServerNetworkHelper.Handler<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(id, (payload, ctx) -> {
            Runnable runnable = handler.handle(ctx.server(), ctx.player(), payload);
            if (runnable != null) ctx.server().execute(runnable);
        });
    }
}