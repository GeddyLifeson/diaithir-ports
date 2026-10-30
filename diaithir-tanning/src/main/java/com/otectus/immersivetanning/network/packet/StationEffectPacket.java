package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


/** A station effect (scraping dust, stitch, splash) that each client spawns with its own particle settings. */
public record StationEffectPacket(BlockPos pos, int kind, float strength) implements CustomPacketPayload {
    public static final Type<StationEffectPacket> TYPE = new Type<>(ImmersiveTanning.id("station_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StationEffectPacket> STREAM_CODEC = StreamCodec.ofMember(StationEffectPacket::encode, StationEffectPacket::decode);

    @Override
    public Type<StationEffectPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(kind);
        buf.writeFloat(strength);
    }

    public static StationEffectPacket decode(RegistryFriendlyByteBuf buf) {
        return new StationEffectPacket(buf.readBlockPos(), buf.readVarInt(), buf.readFloat());
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.stationEffect(this);
    }
}
