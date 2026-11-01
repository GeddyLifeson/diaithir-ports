package rpggods.tameable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import rpggods.util.INBTSerializable;
import rpggods.util.Nbt;
import rpggods.RPGGods;
import rpggods.network.SUpdateSittingPacket;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public interface ITameable extends INBTSerializable<CompoundTag> {

    public static final Identifier REGISTRY_NAME = Identifier.fromNamespaceAndPath(RPGGods.MODID, "tameable");

    static final String OWNER = "Owner";
    static final String SITTING = "Sitting";

    Optional<UUID> getOwnerId();

    boolean isSitting();

    boolean isTamed();

    void setTamed(boolean tamed);

    void setSitting(boolean sitting);

    void setOwnerId(@Nullable UUID ownerId);

    default void setSittingWithUpdate(Entity tamed, boolean isSitting) {
        setSitting(isSitting);
        if(!tamed.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(tamed, new SUpdateSittingPacket(tamed.getId(), isSitting));
        }
    }

    default boolean setTamedBy(Player player) {
        if(!getOwnerId().isPresent()) {
            this.setTamed(true);
            this.setOwnerId(player.getUUID());
            return true;
        }
        return false;
    }

    default Optional<LivingEntity> getOwner(Level world) {
        Optional<UUID> uuid = this.getOwnerId();
        return uuid.isPresent() ? Optional.ofNullable(world.getPlayerByUUID(uuid.get())) : Optional.empty();
    }

    default boolean isOwner(LivingEntity entity) {
        return isTamed() && getOwnerId().isPresent() && getOwnerId().get().equals(entity.getUUID());
    }

    @Override
    default CompoundTag serializeNBT() {
        final CompoundTag nbt = new CompoundTag();
        if(getOwnerId().isPresent()) {
            nbt.store(OWNER, net.minecraft.core.UUIDUtil.CODEC, getOwnerId().get());
        }
        nbt.putBoolean(SITTING, isSitting());
        return nbt;
    }

    @Override
    default void deserializeNBT(final CompoundTag nbt) {
        UUID uuid = nbt.read(OWNER, net.minecraft.core.UUIDUtil.CODEC).orElse(null);

        if (uuid != null) {
            try {
                this.setOwnerId(uuid);
                this.setTamed(true);
            } catch (Throwable throwable) {
                this.setTamed(false);
            }
        }
        setSitting(Nbt.getBoolean(nbt, SITTING));
    }
}
