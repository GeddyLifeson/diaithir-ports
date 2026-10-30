package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.minigame.SessionManager;
import com.otectus.immersivetanning.leather.Finish;
import net.minecraft.core.BlockPos;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;


/** The finish chosen for the wet hide in hand. The server re-checks the hand and the rack. */
public record SelectFinishPacket(BlockPos pos, Finish finish) implements CustomPacketPayload {
    public static final Type<SelectFinishPacket> TYPE = new Type<>(ImmersiveTanning.id("select_finish"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectFinishPacket> STREAM_CODEC = StreamCodec.ofMember(SelectFinishPacket::encode, SelectFinishPacket::decode);

    @Override
    public Type<SelectFinishPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(finish);
    }

    public static SelectFinishPacket decode(RegistryFriendlyByteBuf buf) {
        return new SelectFinishPacket(buf.readBlockPos(), buf.readEnum(Finish.class));
    }

    public void handle(IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> SessionManager.selectFinish(player, pos, finish));
    }
}
