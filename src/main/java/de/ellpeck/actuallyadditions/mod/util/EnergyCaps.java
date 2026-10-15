package de.ellpeck.actuallyadditions.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import javax.annotation.Nullable;

/** Energy capability lookups (26.1 hands out transactional EnergyHandlers), seen as the IEnergyStorage AA's machines use. */
public final class EnergyCaps {
    private EnergyCaps() {}

    @Nullable
    public static IEnergyStorage block(Level level, BlockPos pos, @Nullable Direction side) {
        return wrap(level.getCapability(Capabilities.Energy.BLOCK, pos, side));
    }

    @Nullable
    public static IEnergyStorage item(ItemStack stack) {
        return stack.isEmpty() ? null : wrap(ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM));
    }

    @Nullable
    public static IEnergyStorage wrap(@Nullable EnergyHandler handler) {
        return handler == null ? null : IEnergyStorage.of(handler);
    }
}
