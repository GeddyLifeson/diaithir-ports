package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.hide.HideSize;
import com.otectus.immersivetanning.trait.TraitRef;
import net.minecraft.core.BlockPos;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;


/** Opens the fleshing screen with the seed from which both sides build the same hide. */
public record OpenFleshingPacket(int sessionId, BlockPos pos, HideSize size, long seed, int allowedMs, int readyDelayMs,
                                 @Nullable TraitRef trait) implements CustomPacketPayload {
    public static final Type<OpenFleshingPacket> TYPE = new Type<>(ImmersiveTanning.id("open_fleshing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFleshingPacket> STREAM_CODEC = StreamCodec.ofMember(OpenFleshingPacket::encode, OpenFleshingPacket::decode);

    @Override
    public Type<OpenFleshingPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        buf.writeBlockPos(pos);
        buf.writeEnum(size);
        buf.writeLong(seed);
        buf.writeVarInt(allowedMs);
        buf.writeVarInt(readyDelayMs);
        TraitRef.writeNullable(buf, trait);
    }

    public static OpenFleshingPacket decode(RegistryFriendlyByteBuf buf) {
        return new OpenFleshingPacket(buf.readVarInt(), buf.readBlockPos(), buf.readEnum(HideSize.class), buf.readLong(),
                buf.readVarInt(), buf.readVarInt(), TraitRef.readNullable(buf));
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.openFleshing(this);
    }
}
