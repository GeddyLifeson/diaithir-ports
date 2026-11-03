// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.diaithir.units.fuel.Fuels;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.SimpleStore;
import net.diaithir.vehicles.FuelHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.MoverType;

import java.util.UUID;

/**
 * A two-wheel fuel bowser: a 2,000 L tank on a hitch. A player (or any entity with a tow bar) pulls it; it hangs a fixed hitch length behind the bar, faces it,
 * tilts with the slope and snaps free if the bar gets too far. It carries a fuel store, so pumps, cans and other vehicles move fuel in and out of it.
 * It takes damage and can be repaired with a hammer (see {@link VehicleHealth}).
 */
public class TankTrailer extends Entity implements FuelHolder {
    public static final double CAPACITY_L = 2000;
    public static final double HITCH_BLOCKS = -2.0;
    private static final EntityDataAccessor<Integer> PULLING = SynchedEntityData.defineId(TankTrailer.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(TankTrailer.class, EntityDataSerializers.INT);

    public final SimpleStore tank = new SimpleStore(CAPACITY_L);

    @Override
    public FuelStore fuelStore() {
        return tank;
    }
    public final VehicleHealth health = new VehicleHealth(VehicleHealth.DEFAULT_MAX);
    private Entity pulling;
    private UUID pullingId;

    public TankTrailer(EntityType<? extends TankTrailer> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(PULLING, -1);
        b.define(STAGE, 0);
    }

    public int destroyedStage() {
        return getEntityData().get(STAGE);
    }

    public Entity pulling() {
        return pulling;
    }

    public void setPulling(Entity e) {
        pulling = e;
        pullingId = e == null ? null : e.getUUID();
        if (!level().isClientSide()) getEntityData().set(PULLING, e == null ? -1 : e.getId());
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith(Entity other) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            int id = getEntityData().get(PULLING);
            pulling = id == -1 ? null : level().getEntity(id);
        } else if (pulling == null && pullingId != null && level() instanceof ServerLevel sl) {
            Entity e = sl.getEntity(pullingId);
            if (e != null) setPulling(e);
        }
        Vec3 m = getDeltaMovement();
        setDeltaMovement(m.x, m.y - 0.08, m.z);
        if (!level().isClientSide() && pulling != null) {
            double threshold = Hitch.detachDistance(Config.TRAILER_DETACH.get(), HITCH_BLOCKS);
            if (!pulling.isAlive() || pulling.distanceTo(this) > threshold) {
                level().playSound(null, pulling.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                setPulling(null);
            }
        }
        if (pulling != null) follow(TowPoint.of(pulling));
        else {
            Vec3 d = getDeltaMovement();
            setDeltaMovement(d.x * 0.75, d.y, d.z * 0.75);
            move(MoverType.SELF, getDeltaMovement());
        }
        if (!level().isClientSide()) getEntityData().set(STAGE, health.destroyedStage());
    }

    /** Moves the trailer to hang behind {@code bar}; also what the selftest calls. */
    public void follow(Vec3 bar) {
        Hitch.Pose p = Hitch.follow(bar.x, bar.y, bar.z, getX(), getY(), getZ(), HITCH_BLOCKS, getXRot());
        setYRot(p.yaw());
        Vec3 m = getDeltaMovement();
        setDeltaMovement(p.x() - getX(), m.y, p.z() - getZ());
        move(MoverType.SELF, getDeltaMovement());
        setXRot(p.pitch());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isInvulnerableToBase(source) || !isAlive()) return false;
        if (Config.VEHICLE_DAMAGE.get()) {
            boolean destroyed = health.hurt(amount);
            Entity src = source.getEntity();
            boolean creative = src instanceof Player p && p.isCreative();
            if (creative || destroyed) {
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.AMBIENT, 1.0F, 0.5F);
                remove(RemovalReason.DISCARDED);
            }
        }
        return true;
    }

    @Override
    public boolean causeFallDamage(double distance, float modifier, DamageSource source) {
        float dmg = VehicleHealth.fallDamage(distance, getDeltaMovement().y);
        if (dmg > 0 && Config.VEHICLE_DAMAGE.get() && level() instanceof ServerLevel sl) hurtServer(sl, source, dmg);
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        var f = Fuels.get(in.getStringOr("fuel", ""));
        double q = in.getDoubleOr("litres", 0);
        if (f != null && q > 0) tank.insert(f, q);
        health.set(in.getFloatOr("health", VehicleHealth.DEFAULT_MAX));
        String id = in.getStringOr("pulling", "");
        pullingId = id.isEmpty() ? null : UUID.fromString(id);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        if (tank.fuel() != null) {
            out.putString("fuel", tank.fuel().id());
            out.putDouble("litres", tank.quantity());
        }
        out.putFloat("health", health.health());
        if (pullingId != null && pulling instanceof Player) out.putString("pulling", pullingId.toString()); // only a player link survives a save, as upstream
    }

    @Override
    public boolean shouldBeSaved() {
        return (pulling == null || pulling instanceof Player) && super.shouldBeSaved();
    }
}
