package com.mrh0.createaddition.energy.fe;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/** BlockCapabilityCache for energy that hands out the legacy IEnergyStorage view. */
public final class FeCache {
  private final BlockCapabilityCache<EnergyHandler, Direction> cache;

  private FeCache(BlockCapabilityCache<EnergyHandler, Direction> cache) { this.cache = cache; }

  public static FeCache create(ServerLevel level, BlockPos pos, @Nullable Direction ctx, BooleanSupplier valid, Runnable invalidated) {
    return new FeCache(BlockCapabilityCache.create(Capabilities.Energy.BLOCK, level, pos, ctx, valid, invalidated));
  }

  public @Nullable IEnergyStorage getCapability() { return Fe.wrap(cache.getCapability()); }
}
