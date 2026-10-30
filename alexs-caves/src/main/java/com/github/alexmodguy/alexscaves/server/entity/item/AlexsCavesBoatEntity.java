package com.github.alexmodguy.alexscaves.server.entity.item;

import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.AlexsCavesBoat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AlexsCavesBoatEntity extends Boat implements AlexsCavesBoat {

    private static final EntityDataAccessor<Integer> DATA_ID_AC_BOAT_TYPE = SynchedEntityData.defineId(AlexsCavesBoatEntity.class, EntityDataSerializers.INT);
    private double lastYd;

    public AlexsCavesBoatEntity(EntityType type, Level level) {
        this(type, level, new DropRef(false));
    }

    private static class DropRef implements java.util.function.Supplier<Item> {
        private final boolean chest;
        private AlexsCavesBoat boat;

        private DropRef(boolean chest) {
            this.chest = chest;
        }

        @Override
        public Item get() {
            AlexsCavesBoat.Type type = boat == null ? AlexsCavesBoat.Type.PEWEN : boat.getACBoatType();
            return chest ? type.getChestDropSupplier().get() : type.getDropSupplier().get();
        }
    }

    private AlexsCavesBoatEntity(EntityType type, Level level, DropRef ref) {
        super(type, level, ref);
        ref.boat = this;
        this.blocksBuilding = true;
    }

    public AlexsCavesBoatEntity(Level level, double x, double y, double z) {
        this(ACEntityRegistry.BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public AlexsCavesBoatEntity(Level level, Vec3 location, AlexsCavesBoat.Type type) {
        this(level, location.x, location.y, location.z);
        this.setACBoatType(type);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_AC_BOAT_TYPE, 0);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput nbt) {
        nbt.putString("ACBoatType", getACBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput nbt) {
        if (com.github.alexmodguy.alexscaves.server.misc.ACProps.has(nbt, "ACBoatType")) {
            this.entityData.set(DATA_ID_AC_BOAT_TYPE, AlexsCavesBoat.Type.byName(nbt.getStringOr("ACBoatType", "")).ordinal());
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        this.lastYd = this.getDeltaMovement().y;
        if (!this.isPassenger()) {
            if (onGroundIn) {
                if (this.fallDistance > 3.0F) {
                    if (!this.onGround()) {
                        this.resetFallDistance();
                        return;
                    }

                    this.causeFallDamage(this.fallDistance, 1.0F, this.damageSources().fall());
                    if (!this.level().isClientSide() && !this.isRemoved()) {
                        this.kill((net.minecraft.server.level.ServerLevel) this.level());
                        if (com.github.alexmodguy.alexscaves.server.misc.ACProps.rule(this.level(), net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
                            for (int i = 0; i < 3; ++i) {
                                this.spawnAtLocation((net.minecraft.server.level.ServerLevel) this.level(), this.getACBoatType().getPlankSupplier().get());
                            }

                            for (int j = 0; j < 2; ++j) {
                                this.spawnAtLocation((net.minecraft.server.level.ServerLevel) this.level(), Items.STICK);
                            }
                        }
                    }
                }

                this.resetFallDistance();
            } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && y < 0.0D) {
                this.fallDistance -= (float) y;
            }
        }
    }

    public void setACBoatType(AlexsCavesBoat.Type type) {
        this.entityData.set(DATA_ID_AC_BOAT_TYPE, type.ordinal());
    }

    public AlexsCavesBoat.Type getACBoatType() {
        return AlexsCavesBoat.Type.byId(this.entityData.get(DATA_ID_AC_BOAT_TYPE));
    }

}
