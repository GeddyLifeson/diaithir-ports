package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.hide.HideData;
import com.otectus.immersivetanning.leather.Finish;
import net.minecraft.core.BlockPos;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;


/** Asks the player how to finish the wet hide they are mounting on a rack. */
public record OpenFinishPacket(BlockPos pos, HideData hide, Finish last) implements CustomPacketPayload {
    public static final Type<OpenFinishPacket> TYPE = new Type<>(ImmersiveTanning.id("open_finish"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFinishPacket> STREAM_CODEC = StreamCodec.ofMember(OpenFinishPacket::encode, OpenFinishPacket::decode);

    @Override
    public Type<OpenFinishPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        hide.write(buf);
        buf.writeEnum(last);
    }

    public static OpenFinishPacket decode(RegistryFriendlyByteBuf buf) {
        return new OpenFinishPacket(buf.readBlockPos(), HideData.read(buf), buf.readEnum(Finish.class));
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.openFinish(this);
    }
}
