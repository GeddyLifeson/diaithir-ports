package de.ellpeck.actuallyadditions.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import javax.annotation.Nullable;

/** Fluid capability lookups (26.1 hands out ResourceHandlers), seen as the IFluidHandler AA's machines use. */
public final class FluidCaps {
    private FluidCaps() {}

    @Nullable
    public static IFluidHandler block(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return handler == null ? null : IFluidHandler.of(handler);
    }
}
