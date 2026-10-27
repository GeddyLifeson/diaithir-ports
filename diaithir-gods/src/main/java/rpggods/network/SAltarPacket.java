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
import rpggods.deity.Altar;

import java.util.Map;

/**
 * Called when datapacks are (re)loaded.
 * Sent from the server to the client with a map of
 * Identifier IDs and Altars
 **/
public record SAltarPacket(int index, int total, Map<Identifier, Altar> data) implements CustomPacketPayload {

    public static final Type<SAltarPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "altar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SAltarPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SAltarPacket::index, ByteBufCodecs.VAR_INT, SAltarPacket::total,
                    ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(Identifier.CODEC, Altar.CODEC)), SAltarPacket::data, SAltarPacket::new);

    @Override
    public Type<SAltarPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet when it is received on the client (main thread).
     */
    public static void handlePacket(final SAltarPacket message, final IPayloadContext context) {
        ChunkBuffer.accept(TYPE, message.index(), message.total(), message.data(), RGData::updateAltars);
    }
}
