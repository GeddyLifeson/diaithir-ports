package rpggods.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rpggods.RPGGods;
import rpggods.entity.AltarEntity;

/**
 * Created when an AltarEntity joins the world.
 * The packet sends the AltarEntity inventory to the client
 * to prevent de-sync.
 **/
public record SUpdateAltarPacket(int entityId, ItemStack block) implements CustomPacketPayload {

    public static final Type<SUpdateAltarPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "update_altar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateAltarPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SUpdateAltarPacket::entityId,
            ItemStack.OPTIONAL_STREAM_CODEC, SUpdateAltarPacket::block,
            SUpdateAltarPacket::new);

    @Override
    public Type<SUpdateAltarPacket> type() {
        return TYPE;
    }

    public static void handlePacket(final SUpdateAltarPacket message, final IPayloadContext context) {
        Entity entity = context.player().level().getEntity(message.entityId());
        if (entity instanceof AltarEntity altar) {
            altar.setBlockSlot(message.block());
        }
    }
}
