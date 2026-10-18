package me.desht.pneumaticcraft.common.util.legacy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Wraps a 26.1 block capability cache so callers keep seeing the pre-26.1 handler interfaces.
 */
public class LegacyCache<L> {
    private final BlockCapabilityCache<?, Direction> cache;
    private final Function<Object, L> wrapper;

    @SuppressWarnings("unchecked")
    public <H> LegacyCache(BlockCapabilityCache<H, Direction> cache, Function<H, L> wrapper) {
        this.cache = cache;
        this.wrapper = (Function<Object, L>) (Function<?, L>) wrapper;
    }

    public static LegacyCache<IItemHandler> items(ServerLevel level, BlockPos pos, Direction side, BooleanSupplier isValid, Runnable onInvalid) {
        return new LegacyCache<>(BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, side, isValid, onInvalid), IItemHandler::of);
    }

    public static LegacyCache<IFluidHandler> fluids(ServerLevel level, BlockPos pos, Direction side, BooleanSupplier isValid, Runnable onInvalid) {
        return new LegacyCache<>(BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, level, pos, side, isValid, onInvalid), IFluidHandler::of);
    }

    @Nullable
    public L getCapability() {
        Object handler = cache.getCapability();
        return handler == null ? null : wrapper.apply(handler);
    }
}
