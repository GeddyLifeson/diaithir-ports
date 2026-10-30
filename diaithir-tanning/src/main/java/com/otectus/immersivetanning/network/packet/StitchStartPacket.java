package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.leather.Finish;
import com.otectus.immersivetanning.minigame.StitchPattern;
import com.otectus.immersivetanning.trait.TraitRef;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;


/** The leather is cut: stitching starts after the ready delay with this seam. */
public record StitchStartPacket(int sessionId, StitchPattern pattern, int readyDelayMs, ItemStack result, Finish finish,
                                @Nullable TraitRef trait, int integrity) implements CustomPacketPayload {
    public static final Type<StitchStartPacket> TYPE = new Type<>(ImmersiveTanning.id("stitch_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StitchStartPacket> STREAM_CODEC = StreamCodec.ofMember(StitchStartPacket::encode, StitchStartPacket::decode);

    @Override
    public Type<StitchStartPacket> type() {
        return TYPE;
    }


    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        pattern.write(buf);
        buf.writeVarInt(readyDelayMs);
        ItemStack.STREAM_CODEC.encode(buf, result);
        buf.writeEnum(finish);
        TraitRef.writeNullable(buf, trait);
        buf.writeVarInt(integrity);
    }

    public static StitchStartPacket decode(RegistryFriendlyByteBuf buf) {
        return new StitchStartPacket(buf.readVarInt(), StitchPattern.read(buf), buf.readVarInt(), ItemStack.STREAM_CODEC.decode(buf),
                buf.readEnum(Finish.class), TraitRef.readNullable(buf), buf.readVarInt());
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.stitchStart(this);
    }
}
