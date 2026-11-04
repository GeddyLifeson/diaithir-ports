// Ported from Talk Balloons (LGPL-3.0-only) https://github.com/CERBON-MODS/Talk-Balloons
// Modifications: single vanilla-style custom payload replacing CreateBalloonPacket / Modern Networking
package net.diaithir.hudoverlays.talkballoons.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BalloonPayload(UUID player, Component message, int ticks) implements CustomPacketPayload {
    public static final Type<BalloonPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("diaithirhudoverlays", "talkballoons_balloon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BalloonPayload> CODEC = StreamCodec.composite(
            net.minecraft.core.UUIDUtil.STREAM_CODEC, BalloonPayload::player,
            ComponentSerialization.STREAM_CODEC, BalloonPayload::message,
            ByteBufCodecs.VAR_INT, BalloonPayload::ticks,
            BalloonPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
