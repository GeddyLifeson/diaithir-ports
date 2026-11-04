package com.iafenvoy.jupiter._loader.neoforge.network;

import com.iafenvoy.jupiter.network.ServerNetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class NeoForgeServerNetworkHelper implements ServerNetworkHelper {
    public static final Map<CustomPacketPayload.Type<CustomPacketPayload>, StreamCodec<FriendlyByteBuf, CustomPacketPayload>> TYPES = new HashMap<>();
    public static final Map<CustomPacketPayload.Type<CustomPacketPayload>, ServerNetworkHelper.Handler<CustomPacketPayload>> RECEIVERS = new HashMap<>();

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> void registerPayloadType(CustomPacketPayload.Type<T> id, StreamCodec<FriendlyByteBuf, T> codec) {
        TYPES.put((CustomPacketPayload.Type<CustomPacketPayload>) id, (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) codec);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> id, ServerNetworkHelper.Handler<T> handler) {
        RECEIVERS.put((CustomPacketPayload.Type<CustomPacketPayload>) id, (ServerNetworkHelper.Handler<CustomPacketPayload>) handler);
    }

    public static void handleData(CustomPacketPayload payload, IPayloadContext ctx) {
        RECEIVERS.entrySet().stream().filter(x -> x.getKey().id().equals(payload.type().id())).map(e -> e.getValue().handle(((ServerLevel) ctx.player().level()).getServer(), (ServerPlayer) ctx.player(), payload)).filter(Objects::nonNull).forEach(Runnable::run);
    }
}