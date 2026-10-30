package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.chat.Component;


/** The server ended a session (station gone, tool changed, moved away...) and says why. */
public record CloseSessionPacket(int sessionId, Component reason) implements CustomPacketPayload {
    public static final Type<CloseSessionPacket> TYPE = new Type<>(ImmersiveTanning.id("close_session"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CloseSessionPacket> STREAM_CODEC = StreamCodec.ofMember(CloseSessionPacket::encode, CloseSessionPacket::decode);

    @Override
    public Type<CloseSessionPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.encode(buf, reason);
    }

    public static CloseSessionPacket decode(RegistryFriendlyByteBuf buf) {
        return new CloseSessionPacket(buf.readVarInt(), net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.decode(buf));
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.closeSession(this);
    }
}
