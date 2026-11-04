package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.minigame.SessionManager;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;


/** The player declares the hide done before the time runs out. */
public record FinishFleshingPacket(int sessionId) implements CustomPacketPayload {
    public static final Type<FinishFleshingPacket> TYPE = new Type<>(ImmersiveTanning.id("finish_fleshing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FinishFleshingPacket> STREAM_CODEC = StreamCodec.ofMember(FinishFleshingPacket::encode, FinishFleshingPacket::decode);

    @Override
    public Type<FinishFleshingPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
    }

    public static FinishFleshingPacket decode(RegistryFriendlyByteBuf buf) {
        return new FinishFleshingPacket(buf.readVarInt());
    }

    public void handle(IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> SessionManager.finishFleshingEarly(player, sessionId));
    }
}
