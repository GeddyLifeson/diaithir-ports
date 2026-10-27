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
import rpggods.deity.Sacrifice;

import java.util.Map;

/**
 * Called when datapacks are (re)loaded.
 * Sent from the server to the client with a map of
 * Identifier IDs and Sacrifices
 **/
public record SSacrificePacket(Map<Identifier, Sacrifice> data) implements CustomPacketPayload {

    public static final Type<SSacrificePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "sacrifice"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SSacrificePacket> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(Codec.unboundedMap(Identifier.CODEC, Sacrifice.CODEC))
                    .map(SSacrificePacket::new, SSacrificePacket::data);

    @Override
    public Type<SSacrificePacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet when it is received on the client (main thread).
     */
    public static void handlePacket(final SSacrificePacket message, final IPayloadContext context) {
        RGData.updateSacrifices(message.data());
    }
}
