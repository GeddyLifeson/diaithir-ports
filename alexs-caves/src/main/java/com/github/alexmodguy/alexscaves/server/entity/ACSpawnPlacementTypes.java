package com.github.alexmodguy.alexscaves.server.entity;

import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;

/**
 * Custom spawn placement types for Alex's Caves fluids.
 * These replace the 1.20 Forge-style custom SpawnPlacements that are no longer supported in 1.21.
 */
public class ACSpawnPlacementTypes {

    /**
     * Spawn placement type for entities that spawn in acid fluid.
     * Used by RadgillEntity in Toxic Caves.
     */
    public static final SpawnPlacementType IN_ACID = (level, pos, entityType) -> {
        if (entityType != null && level.getWorldBorder().isWithinBounds(pos)) {
            FluidState fluidState = level.getFluidState(pos);
            BlockPos blockpos = pos.above();
            return fluidState.getFluidType() == ACFluidRegistry.ACID_FLUID_TYPE.get() 
                    && !level.getBlockState(blockpos).isRedstoneConductor(level, blockpos);
        }
        return false;
    };

    /**
     * Spawn placement type for entities that spawn in purple soda fluid.
     * Used by SweetishFishEntity in Candy Cavity.
     */
    public static final SpawnPlacementType IN_PURPLE_SODA = (level, pos, entityType) -> {
        if (entityType != null && level.getWorldBorder().isWithinBounds(pos)) {
            FluidState fluidState = level.getFluidState(pos);
            BlockPos blockpos = pos.above();
            return fluidState.getFluidType() == ACFluidRegistry.PURPLE_SODA_FLUID_TYPE.get() 
                    && !level.getBlockState(blockpos).isRedstoneConductor(level, blockpos);
        }
        return false;
    };
}
