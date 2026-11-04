package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.setup.registry.DataSerializers;
import net.diaithir.druidry.ars.api.entity.ISummon;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.minecraft.util.Util;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class SummonHorse extends Horse implements ISummon {
    public int ticksLeft;
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(SummonHorse.class, DataSerializers.OPTIONAL_UUID.get());

    public SummonHorse(EntityType<? extends Horse> type, Level worldIn) {
        super(type, worldIn);
    }

    protected boolean canParent() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player p_230254_1_, InteractionHand p_230254_2_) {
        return super.mobInteract(p_230254_1_, p_230254_2_);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(OWNER_UUID, Optional.of(Util.NIL_UUID));
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

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return ISummon.super.getOwner();
    }

    @Override
    public Level level() {
        return super.level();
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        onSummonDeath(level(), cause, false);
    }

    @Override
    public boolean canHoldItem(ItemStack itemstackIn) {
        return false;
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
    }

    @Override
    public int getBaseExperienceReward(ServerLevel level) {
        return 0;
    }

    public SimpleContainer getHorseInventory() {
        return this.inventory;
    }

    @Override
    public void openCustomInventoryScreen(Player playerEntity) {
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return false;
    }

    public boolean canBreed() {
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

    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("left", ticksLeft);
        writeOwner(compound);
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
