package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.minigame.SessionManager;
import net.minecraft.util.Util;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;


/** One press of the awl: a sequence number and the client session time. */
public record StitchPressPacket(int sessionId, int sequence, int clientMs) implements CustomPacketPayload {
    public static final Type<StitchPressPacket> TYPE = new Type<>(ImmersiveTanning.id("stitch_press"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StitchPressPacket> STREAM_CODEC = StreamCodec.ofMember(StitchPressPacket::encode, StitchPressPacket::decode);

    @Override
    public Type<StitchPressPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        buf.writeVarInt(sequence);
        buf.writeVarInt(Math.max(0, clientMs));
    }

    public static StitchPressPacket decode(RegistryFriendlyByteBuf buf) {
        return new StitchPressPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(IPayloadContext ctx) {
        long arrival = Util.getMillis();
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> SessionManager.stitchPress(player, sessionId, sequence, clientMs, arrival));
    }
}
