package net.diaithir.druidry.ars.api.entity;

import net.diaithir.druidry.ars.api.event.SummonEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * For entities summoned by spells.
 */
public interface ISummon extends OwnableEntity {
    // How many ticks the summon has left
    int getTicksLeft();

    void setTicksLeft(int ticks);

    default @Nullable LivingEntity getLivingEntity() {
        return this instanceof LivingEntity ? (LivingEntity) this : null;
    }

    void setOwnerID(UUID uuid);

    default void onSummonDeath(Level world, @Nullable DamageSource source, boolean didExpire) {
        NeoForge.EVENT_BUS.post(new SummonEvent.Death(world, this, source, didExpire));
    }

    /** 26.1: OwnableEntity only exposes an EntityReference; this keeps the old UUID accessor. */
    default @Nullable UUID getOwnerUUID() {
        var ref = getOwnerReference();
        return ref == null ? null : ref.getUUID();
    }

    default void writeOwner(CompoundTag tag) {
        if (getOwnerUUID() != null)
            tag.putIntArray("owner", UUIDUtil.uuidToIntArray(getOwnerUUID()));
    }

    default @Nullable Entity readOwner(ServerLevel world, CompoundTag tag) {
        return tag.getIntArray("owner").map(arr -> world.getEntity(UUIDUtil.uuidFromIntArray(arr))).orElse(null);
    }

    /*
     * This is a workaround for summon entities that inherit from an entity that override LivingEntity.getOwner() to return a subclass that doesn't include players. Ex. Vex
     * By default it will redirect to the classic getOwner() method, so it's safer to use as getter for general purpose
     * */
    default LivingEntity getOwnerAlt() {
        return getOwner();
    }
}
