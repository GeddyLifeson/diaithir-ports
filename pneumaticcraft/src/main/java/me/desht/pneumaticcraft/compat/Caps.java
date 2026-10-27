package me.desht.pneumaticcraft.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * Lookups of other blocks' item / fluid / energy capabilities, returning the 1.21-style handler interfaces
 * this mod still uses internally.
 */
public final class Caps {
    private Caps() {}

    /** Markers used by SideConfigurator to identify the type of a handler. */
    public static final Object ITEM = "item", FLUID = "fluid", ENERGY = "energy";

    @Nullable
    public static IItemHandler item(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        return h == null ? null : IItemHandler.of(h);
    }

    @Nullable
    public static IItemHandler item(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity be, @Nullable Direction side) {
        ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos, state, be, side);
        return h == null ? null : IItemHandler.of(h);
    }

    @Nullable
    public static IFluidHandler fluid(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<FluidResource> h = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return h == null ? null : IFluidHandler.of(h);
    }

    @Nullable
    public static IFluidHandler fluid(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity be, @Nullable Direction side) {
        ResourceHandler<FluidResource> h = level.getCapability(Capabilities.Fluid.BLOCK, pos, state, be, side);
        return h == null ? null : IFluidHandler.of(h);
    }

    @Nullable
    public static IEnergyStorage energy(Level level, BlockPos pos, @Nullable Direction side) {
        EnergyHandler h = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
        return h == null ? null : IEnergyStorage.of(h);
    }

    @Nullable
    public static IEnergyStorage energy(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity be, @Nullable Direction side) {
        EnergyHandler h = level.getCapability(Capabilities.Energy.BLOCK, pos, state, be, side);
        return h == null ? null : IEnergyStorage.of(h);
    }

    @Nullable
    public static net.neoforged.neoforge.fluids.capability.IFluidHandlerItem fluidItem(net.minecraft.world.item.ItemStack stack) {
        var access = net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack);
        ResourceHandler<FluidResource> h = stack.getCapability(Capabilities.Fluid.ITEM, access);
        return h == null ? null : new net.neoforged.neoforge.fluids.capability.FluidResourceHandlerItemAdapter(h, access);
    }

    @Nullable
    public static IItemHandler itemItem(net.minecraft.world.item.ItemStack stack) {
        ResourceHandler<ItemResource> h = stack.getCapability(Capabilities.Item.ITEM, net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack));
        return h == null ? null : IItemHandler.of(h);
    }

    @Nullable
    public static IEnergyStorage energyItem(net.minecraft.world.item.ItemStack stack) {
        EnergyHandler h = stack.getCapability(Capabilities.Energy.ITEM, net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack));
        return h == null ? null : IEnergyStorage.of(h);
    }

    @Nullable
    public static IItemHandler itemEntity(Entity entity) {
        ResourceHandler<ItemResource> h = entity.getCapability(Capabilities.Item.ENTITY);
        return h == null ? null : IItemHandler.of(h);
    }

    @Nullable
    public static IItemHandler itemEntityAutomation(Entity entity, @Nullable Direction side) {
        ResourceHandler<ItemResource> h = entity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, side);
        return h == null ? null : IItemHandler.of(h);
    }

    @Nullable
    public static IFluidHandler fluidEntity(Entity entity, @Nullable Direction side) {
        ResourceHandler<FluidResource> h = entity.getCapability(Capabilities.Fluid.ENTITY, side);
        return h == null ? null : IFluidHandler.of(h);
    }
}
