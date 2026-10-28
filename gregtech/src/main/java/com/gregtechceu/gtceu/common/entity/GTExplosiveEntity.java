package com.gregtechceu.gtceu.common.entity;



import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GTExplosiveEntity extends PrimedTnt {

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type,
                             Level level, double x, double y, double z,
                             @Nullable LivingEntity owner) {
        this(type, level);
        this.setPos(x, y, z);
        double d = level.getRandom().nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.owner = owner == null ? null : net.minecraft.world.entity.EntityReference.of(owner);
    }

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type, Level level) {
        super(type, level);
    }

    /**
     * @return The strength of the explosive.
     */
    protected abstract float getStrength();

    /**
     * @return Whether to drop all blocks, or use default logic
     */
    public abstract boolean dropsAllBlocks();

    /**
     * @return The range of the explosive, if {@link #dropsAllBlocks} is true.
     */
    protected int getRange() {
        return 2;
    }

    /**
     * @return The block state of the block this explosion entity is created by.
     */
    public abstract @NotNull BlockState getExplosiveState();

    @Override
    protected void explode() {
        explode(level(), this, this.getX(), this.getY(0.0625), this.getZ(), getStrength(), dropsAllBlocks());
    }

    protected void explode(Level level, @Nullable Entity source, double x, double y, double z,
                           float radius, boolean dropBlocks) {  /* ported stub */ }
}
