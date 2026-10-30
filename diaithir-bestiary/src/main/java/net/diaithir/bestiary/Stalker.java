package net.diaithir.bestiary;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A horror that only moves while no player is looking at it. While watched it cannot move, turn or attack. */
public class Stalker extends Creature {
    private static final EntityDataAccessor<Boolean> WATCHED = SynchedEntityData.defineId(Stalker.class, EntityDataSerializers.BOOLEAN);
    private static final double SIGHT = 64;

    public Stalker(EntityType<? extends Stalker> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(WATCHED, false);
    }

    public boolean watched() {
        return entityData.get(WATCHED);
    }

    public boolean isWatchedBy(Player player) {
        return player.isAlive() && !player.isSpectator() && distanceToSqr(player) < SIGHT * SIGHT
                && isLookingAtMe(player, 0.4, false, true, getEyeY(), getY() + getBbHeight() * 0.5);
    }

    @Override
    public void aiStep() {
        if (!level().isClientSide()) {
            boolean seen = level().players().stream().anyMatch(this::isWatchedBy);
            if (seen != watched()) entityData.set(WATCHED, seen);
            if (seen) {
                navigation.stop();
                setDeltaMovement(0, Math.min(getDeltaMovement().y, 0), 0);
            }
        }
        super.aiStep();
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || watched();
    }

    @Override
    protected boolean paused() {
        return watched();
    }
}
