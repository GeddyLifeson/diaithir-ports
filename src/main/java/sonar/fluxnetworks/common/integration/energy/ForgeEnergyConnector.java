package sonar.fluxnetworks.common.integration.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.energy.IBlockEnergyConnector;
import sonar.fluxnetworks.api.energy.IItemEnergyConnector;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForgeEnergyConnector implements IBlockEnergyConnector, IItemEnergyConnector {

    public static final ForgeEnergyConnector INSTANCE = new ForgeEnergyConnector();

    private ForgeEnergyConnector() {
    }

    @Nullable
    private static IEnergyStorage get(@Nonnull BlockEntity target, @Nonnull Direction side) {
        EnergyHandler handler = FluxUtils.get(target, Capabilities.Energy.BLOCK, side);
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    @Nullable
    private static IEnergyStorage get(@Nonnull ItemStack stack) {
        EnergyHandler handler = ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    @Override
    public boolean hasCapability(@Nonnull BlockEntity target, @Nonnull Direction side) {
        return !target.isRemoved() && get(target, side) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            IEnergyStorage storage = get(target, side);
            return storage != null && storage.canReceive();
        }
        return false;
    }

    @Override
    public boolean canReceiveFrom(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            IEnergyStorage storage = get(target, side);
            return storage != null && storage.canExtract();
        }
        return false;
    }

    @Override
    public long sendTo(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        IEnergyStorage storage = get(target, side);
        return storage == null ? 0 : storage.receiveEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        IEnergyStorage storage = get(target, side);
        return storage == null ? 0 : storage.extractEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }

    @Override
    public boolean hasCapability(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && get(stack) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            IEnergyStorage storage = get(stack);
            return storage != null && storage.canReceive();
        }
        return false;
    }

    @Override
    public boolean canReceiveFrom(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            IEnergyStorage storage = get(stack);
            return storage != null && storage.canExtract();
        }
        return false;
    }

    @Override
    public long sendTo(long amount, @Nonnull ItemStack stack, boolean simulate) {
        IEnergyStorage storage = get(stack);
        return storage == null ? 0 : storage.receiveEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull ItemStack stack, boolean simulate) {
        IEnergyStorage storage = get(stack);
        return storage == null ? 0 : storage.extractEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }
}
