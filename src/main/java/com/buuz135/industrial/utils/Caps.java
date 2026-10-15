package com.buuz135.industrial.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.FluidResourceHandlerItemAdapter;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;

/** Capability lookups: 26.1 hands out transactional handlers, seen here as the legacy handlers IF's machines use. */
public final class Caps {
    private Caps() {}

    @Nullable
    public static IFluidHandler fluidBlock(Level level, BlockPos pos, @Nullable Direction side) {
        var handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return handler == null ? null : IFluidHandler.of(handler);
    }

    @Nullable
    public static IFluidHandlerItem fluidItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ItemAccess access = ItemAccess.forStack(stack);
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        return handler == null ? null : new FluidResourceHandlerItemAdapter(handler, access);
    }

    @Nullable
    public static IEnergyStorage energyBlock(Level level, BlockPos pos, @Nullable Direction side) {
        var handler = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    @Nullable
    public static IEnergyStorage energyItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var handler = ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
        return handler == null ? null : IEnergyStorage.of(handler);
    }
}
