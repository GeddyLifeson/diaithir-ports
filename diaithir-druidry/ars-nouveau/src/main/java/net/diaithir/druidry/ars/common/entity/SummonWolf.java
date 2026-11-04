package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.setup.registry.DataSerializers;
import net.diaithir.druidry.ars.api.entity.ISummon;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.minecraft.util.Util;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;
import java.util.UUID;

public class SummonWolf extends Wolf implements ISummon {
    public int ticksLeft;
    public boolean isWildenSummon;

    public SummonWolf(EntityType<? extends Wolf> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            ticksLeft--;
            if (ticksLeft <= 0) {
                ParticleUtil.spawnPoof((ServerLevel) level(), blockPosition());
                this.remove(Entity.RemovalReason.DISCARDED);
                onSummonDeath(level(), null, true);
            }
        }
    }

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(SummonWolf.class, DataSerializers.OPTIONAL_UUID.get());

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(OWNER_UUID, Optional.of(Util.NIL_UUID));
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        onSummonDeath(level(), cause, false);
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public boolean canMate(Animal pOtherAnimal) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        this.ticksLeft = compound.getIntOr("left", 0);
        this.isWildenSummon = compound.getBooleanOr("wildenSummon", false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("left", ticksLeft);
        compound.putBoolean("wildenSummon", isWildenSummon);
    }

    @Override
    public int getBaseExperienceReward(ServerLevel level) {
        return 0;
    }

    @Override
    public int getTicksLeft() {
        return ticksLeft;
    }

    @Override
    public void setTicksLeft(int ticks) {
        this.ticksLeft = ticks;
    }


    @org.jetbrains.annotations.Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.getEntityData().get(OWNER_UUID).isEmpty() ? this.getUUID() : this.getEntityData().get(OWNER_UUID).get();
    }

    @Override
    public void setOwnerID(UUID uuid) {
        this.getEntityData().set(OWNER_UUID, Optional.ofNullable(uuid));
    }
}
