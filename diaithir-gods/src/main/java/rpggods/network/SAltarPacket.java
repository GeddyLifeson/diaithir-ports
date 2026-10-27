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
public record SAltarPacket(Map<Identifier, Altar> data) implements CustomPacketPayload {

    public static final Type<SAltarPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "altar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SAltarPacket> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(Identifier.CODEC, Altar.CODEC))
                    .map(SAltarPacket::new, SAltarPacket::data);

    @Override
    public Type<SAltarPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet when it is received on the client (main thread).
     */
    public static void handlePacket(final SAltarPacket message, final IPayloadContext context) {
        RGData.updateAltars(message.data());
    }
}
