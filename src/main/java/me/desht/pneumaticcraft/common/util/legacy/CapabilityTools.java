package me.desht.pneumaticcraft.common.util.legacy;

import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

public class CapabilityTools {

    // 26.1 capabilities are transfer-API handlers. McJtyLib code works with the pre-26.1 interfaces, so lookups are
    // wrapped on the way in and McJtyLib's own handlers are wrapped on the way out (expose).

    @Nullable
    public static IEnergyStorage energy(Level level, BlockPos pos, @Nullable Direction side) {
        EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    @Nullable
    public static IEnergyStorage energy(ItemStack stack) {
        EnergyHandler handler = ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    @Nullable
    public static IItemHandler items(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        return handler == null ? null : IItemHandler.of(handler);
    }

    @Nullable
    public static IFluidHandler fluids(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return handler == null ? null : IFluidHandler.of(handler);
    }

    @Nullable
    public static ResourceHandler<ItemResource> wrapItems(@Nullable IItemHandler h) {
        return h == null ? null : new LegacyItemHandler(h);
    }

    @Nullable
    public static ResourceHandler<FluidResource> wrapFluids(@Nullable IFluidHandler h) {
        return h == null ? null : new LegacyFluidHandler(h);
    }

    @Nullable
    public static EnergyHandler wrapEnergy(@Nullable IEnergyStorage h) {
        return h == null ? null : new LegacyEnergyHandler(h);
    }

    public static Object expose(Object handler) {
        if (handler instanceof IItemHandler items) {
            return new LegacyItemHandler(items);
        }
        if (handler instanceof IEnergyStorage energy) {
            return new LegacyEnergyHandler(energy);
        }
        if (handler instanceof IFluidHandler fluids) {
            return new LegacyFluidHandler(fluids);
        }
        return handler;
    }

    @Nullable
    public static IItemHandler items(ItemStack stack) {
        ResourceHandler<ItemResource> handler = ItemAccess.forStack(stack).getCapability(Capabilities.Item.ITEM);
        return handler == null ? null : IItemHandler.of(handler);
    }

    @Nullable
    public static IFluidHandler fluids(ItemStack stack) {
        ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
        return handler == null ? null : IFluidHandler.of(handler);
    }

    @Nullable
    public static IItemHandler getItemCapabilitySafe(BlockEntity tileEntity) {
        if (tileEntity != null && tileEntity.getLevel() != null) {
            return items(tileEntity.getLevel(), tileEntity.getBlockPos(), null);
        }
        return null;
    }

    @Nullable
    public static IFluidHandler getFluidCapabilitySafe(BlockEntity tileEntity) {
        if (tileEntity != null && tileEntity.getLevel() != null) {
            return fluids(tileEntity.getLevel(), tileEntity.getBlockPos(), null);
        }
        return null;
    }
}
