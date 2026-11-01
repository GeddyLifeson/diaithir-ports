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
import rpggods.deity.Deity;

import java.util.Map;

/**
 * Called when datapacks are (re)loaded.
 * Sent from the server to the client with a map of
 * Identifier IDs and Deitys
 **/
public record SDeityPacket(int index, int total, Map<Identifier, Deity> data) implements CustomPacketPayload {

    public static final Type<SDeityPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "deity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SDeityPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SDeityPacket::index, ByteBufCodecs.VAR_INT, SDeityPacket::total,
                    ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(Identifier.CODEC, Deity.CODEC)), SDeityPacket::data, SDeityPacket::new);

    @Override
    public Type<SDeityPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet when it is received on the client (main thread).
     */
    public static void handlePacket(final SDeityPacket message, final IPayloadContext context) {
        ChunkBuffer.accept(TYPE, message.index(), message.total(), message.data(), RGData::updateDeities);
    }
}
