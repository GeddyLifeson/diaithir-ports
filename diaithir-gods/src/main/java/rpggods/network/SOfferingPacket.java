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
import rpggods.deity.Offering;

import java.util.Map;

/**
 * Called when datapacks are (re)loaded.
 * Sent from the server to the client with a map of
 * Identifier IDs and Offerings
 **/
public record SOfferingPacket(Map<Identifier, Offering> data) implements CustomPacketPayload {

    public static final Type<SOfferingPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "offering"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SOfferingPacket> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(Identifier.CODEC, Offering.CODEC))
                    .map(SOfferingPacket::new, SOfferingPacket::data);

    @Override
    public Type<SOfferingPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet when it is received on the client (main thread).
     */
    public static void handlePacket(final SOfferingPacket message, final IPayloadContext context) {
        RGData.updateOfferings(message.data());
    }
}
