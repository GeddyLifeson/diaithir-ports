package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.minigame.SessionManager;
import net.minecraft.core.BlockPos;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;


/** From the fleshing result: mount the next raw hide from the inventory on the same rack and flesh it. */
public record FleshNextPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<FleshNextPacket> TYPE = new Type<>(ImmersiveTanning.id("flesh_next"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FleshNextPacket> STREAM_CODEC = StreamCodec.ofMember(FleshNextPacket::encode, FleshNextPacket::decode);

    @Override
    public Type<FleshNextPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static FleshNextPacket decode(RegistryFriendlyByteBuf buf) {
        return new FleshNextPacket(buf.readBlockPos());
    }

    public void handle(IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> SessionManager.fleshNext(player, pos));
    }
}
