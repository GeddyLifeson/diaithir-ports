package rpggods.network;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rpggods.RGData;
import rpggods.RPGGods;
import rpggods.perk.Perk;

import java.util.Map;

/**
 * Called when datapacks are (re)loaded.
 * Sent from the server to the client with a map of
 * Identifier IDs and Perks
 **/
public record SPerkPacket(int index, int total, Map<Identifier, Perk> data) implements CustomPacketPayload {

    public static final Type<SPerkPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "perk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SPerkPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SPerkPacket::index, ByteBufCodecs.VAR_INT, SPerkPacket::total,
                    ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(Identifier.CODEC, Perk.CODEC)), SPerkPacket::data, SPerkPacket::new);

    @Override
    public Type<SPerkPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet when it is received on the client (main thread).
     */
    public static void handlePacket(final SPerkPacket message, final IPayloadContext context) {
        ChunkBuffer.accept(TYPE, message.index(), message.total(), message.data(), RGData::updatePerks);
    }
}
