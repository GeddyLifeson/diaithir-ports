package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


/** The server's verdict on one press, so the client can correct its prediction. {@code index < 0}: ignored. */
public record StitchResultPacket(int sessionId, int sequence, int index, float accuracy, int next) implements CustomPacketPayload {
    public static final Type<StitchResultPacket> TYPE = new Type<>(ImmersiveTanning.id("stitch_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StitchResultPacket> STREAM_CODEC = StreamCodec.ofMember(StitchResultPacket::encode, StitchResultPacket::decode);

    @Override
    public Type<StitchResultPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        buf.writeVarInt(sequence);
        buf.writeVarInt(index + 1);
        buf.writeFloat(accuracy);
        buf.writeVarInt(next);
    }

    public static StitchResultPacket decode(RegistryFriendlyByteBuf buf) {
        return new StitchResultPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt() - 1, buf.readFloat(), buf.readVarInt());
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.stitchResult(this);
    }
}
