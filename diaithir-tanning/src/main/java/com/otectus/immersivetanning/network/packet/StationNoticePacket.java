package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.chat.Component;


/**
 * A station message (status, instruction or failure). The client keeps it readable above the hotbar for its configured
 * time instead of letting the next action-bar message replace it; {@code failure} marks problems the player must act on.
 */
public record StationNoticePacket(Component message, boolean failure) implements CustomPacketPayload {
    public static final Type<StationNoticePacket> TYPE = new Type<>(ImmersiveTanning.id("station_notice"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StationNoticePacket> STREAM_CODEC = StreamCodec.ofMember(StationNoticePacket::encode, StationNoticePacket::decode);

    @Override
    public Type<StationNoticePacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.encode(buf, message);
        buf.writeBoolean(failure);
    }

    public static StationNoticePacket decode(RegistryFriendlyByteBuf buf) {
        return new StationNoticePacket(net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.decode(buf), buf.readBoolean());
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.stationNotice(this);
    }
}
