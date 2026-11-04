package rpggods.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rpggods.RPGGods;

/**
 * Sent when a mob with Tameable data changes its sitting state
 **/
public record SUpdateSittingPacket(int entityId, boolean sitting) implements CustomPacketPayload {

    public static final Type<SUpdateSittingPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "update_sitting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateSittingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SUpdateSittingPacket::entityId,
            ByteBufCodecs.BOOL, SUpdateSittingPacket::sitting,
            SUpdateSittingPacket::new);

    @Override
    public Type<SUpdateSittingPacket> type() {
        return TYPE;
    }

    public static void handlePacket(final SUpdateSittingPacket message, final IPayloadContext context) {
        Entity entity = context.player().level().getEntity(message.entityId());
        if (entity != null) {
            RPGGods.getTameable(entity).ifPresent(t -> t.setSitting(message.sitting()));
        }
    }
}
