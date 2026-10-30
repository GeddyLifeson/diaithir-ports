package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.minigame.SessionManager;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;


/** The player closed a session screen before it finished. */
public record CancelSessionPacket(int sessionId) implements CustomPacketPayload {
    public static final Type<CancelSessionPacket> TYPE = new Type<>(ImmersiveTanning.id("cancel_session"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelSessionPacket> STREAM_CODEC = StreamCodec.ofMember(CancelSessionPacket::encode, CancelSessionPacket::decode);

    @Override
    public Type<CancelSessionPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
    }

    public static CancelSessionPacket decode(RegistryFriendlyByteBuf buf) {
        return new CancelSessionPacket(buf.readVarInt());
    }

    public void handle(IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> SessionManager.cancelFromClient(player, sessionId));
    }
}
