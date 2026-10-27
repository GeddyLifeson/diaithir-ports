package me.desht.pneumaticcraft.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A capability cache which hands out the 1.21-style handler interfaces this mod uses internally.
 */
public final class HandlerCache<T> {
    private final Supplier<@Nullable T> getter;

    private HandlerCache(Supplier<@Nullable T> getter) {
        this.getter = getter;
    }

    @Nullable
    public T getCapability() {
        return getter.get();
    }

    public static HandlerCache<IItemHandler> item(ServerLevel level, BlockPos pos, @Nullable Direction side, BooleanSupplier valid, Runnable invalidate) {
        BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> c = BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, side, valid, invalidate);
        return new HandlerCache<>(() -> {
            ResourceHandler<ItemResource> h = c.getCapability();
            return h == null ? null : IItemHandler.of(h);
        });
    }

    public static HandlerCache<IFluidHandler> fluid(ServerLevel level, BlockPos pos, @Nullable Direction side, BooleanSupplier valid, Runnable invalidate) {
        BlockCapabilityCache<ResourceHandler<FluidResource>, Direction> c = BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, level, pos, side, valid, invalidate);
        return new HandlerCache<>(() -> {
            ResourceHandler<FluidResource> h = c.getCapability();
            return h == null ? null : IFluidHandler.of(h);
        });
    }
}
