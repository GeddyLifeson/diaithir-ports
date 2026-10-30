package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.minigame.TanningSession;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


/**
 * Final server-computed result. For fleshing, {@code score} is the hide's Integrity and the three measures explain it;
 * for stitching it is the Workmanship.
 */
public record SessionResultPacket(int sessionId, TanningSession.Kind kind, int score, float coverage, float control,
                                  float overwork, boolean timedOut) implements CustomPacketPayload {
    public static final Type<SessionResultPacket> TYPE = new Type<>(ImmersiveTanning.id("session_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SessionResultPacket> STREAM_CODEC = StreamCodec.ofMember(SessionResultPacket::encode, SessionResultPacket::decode);

    @Override
    public Type<SessionResultPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        buf.writeEnum(kind);
        buf.writeVarInt(score);
        buf.writeFloat(coverage);
        buf.writeFloat(control);
        buf.writeFloat(overwork);
        buf.writeBoolean(timedOut);
    }

    public static SessionResultPacket decode(RegistryFriendlyByteBuf buf) {
        return new SessionResultPacket(buf.readVarInt(), buf.readEnum(TanningSession.Kind.class), buf.readVarInt(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.sessionResult(this);
    }
}
