package com.iafenvoy.jupiter._loader.neoforge.network;

import com.iafenvoy.jupiter.network.ClientNetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class NeoForgeClientNetworkHelper implements ClientNetworkHelper {
    public static final Map<CustomPacketPayload.Type<CustomPacketPayload>, ClientNetworkHelper.Handler<CustomPacketPayload>> RECEIVERS = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> id, Handler<T> handler) {
        RECEIVERS.put((CustomPacketPayload.Type<CustomPacketPayload>) id, (ClientNetworkHelper.Handler<CustomPacketPayload>) handler);
    }

    public static void handleData(CustomPacketPayload payload, IPayloadContext ctx) {
        RECEIVERS.entrySet().stream().filter(x -> x.getKey().id().equals(payload.type().id())).map(e -> e.getValue().handle(Minecraft.getInstance(), payload)).filter(Objects::nonNull).forEach(Runnable::run);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}