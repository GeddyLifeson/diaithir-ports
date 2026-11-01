package rpggods.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rpggods.RPGGods;
import rpggods.entity.AltarEntity;
import rpggods.util.altar.AltarPose;

/**
 * Created when the player closes an AltarScreen GUI.
 * The packet sends the AltarPose, name, and other settings
 * to the server to update the AltarEntity NBT data.
 **/
public record CUpdateAltarPacket(int entityId, CompoundTag pose, boolean female, boolean slim, String customName) implements CustomPacketPayload {

    protected static final int NAME_LEN = 50;

    public static final Type<CUpdateAltarPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RPGGods.MODID, "c_update_altar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CUpdateAltarPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CUpdateAltarPacket::entityId,
            ByteBufCodecs.COMPOUND_TAG, CUpdateAltarPacket::pose,
            ByteBufCodecs.BOOL, CUpdateAltarPacket::female,
            ByteBufCodecs.BOOL, CUpdateAltarPacket::slim,
            ByteBufCodecs.stringUtf8(NAME_LEN), CUpdateAltarPacket::customName,
            CUpdateAltarPacket::new);

    /**
     * @param entityId    the ID of the altar entity
     * @param pose        the StatuePose settings
     * @param female      true if the statue uses the female model
     * @param slim        true if the statue uses the slim model
     * @param customName the String name of the texture to use
     **/
    public static CUpdateAltarPacket of(final int entityId, final AltarPose pose,
                                        final boolean female, final boolean slim, final String customName) {
        String name = customName == null ? "" : customName;
        if (name.length() > NAME_LEN) {
            name = name.substring(0, NAME_LEN);
        }
        return new CUpdateAltarPacket(entityId, pose.serializeNBT(), female, slim, name);
    }

    @Override
    public Type<CUpdateAltarPacket> type() {
        return TYPE;
    }

    public static void handlePacket(final CUpdateAltarPacket message, final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Entity entity = player.level().getEntity(message.entityId());
        if (entity instanceof AltarEntity altar && player.isWithinEntityInteractionRange(entity, 1.0D)) {
            // update pose and name
            altar.setAltarPose(new AltarPose(message.pose()));
            if (message.customName() != null && !message.customName().isEmpty()) {
                altar.setCustomName(Component.literal(message.customName()));
            }
        }
    }
}
