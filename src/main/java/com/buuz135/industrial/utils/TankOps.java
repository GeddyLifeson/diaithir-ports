package com.buuz135.industrial.utils;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;

/** Titanium's 26.1 tanks are transactional; these keep IF's simulate/execute call sites, committing only on EXECUTE. */
public final class TankOps {
    private TankOps() {}

    public static int fill(ResourceHandler<FluidResource> tank, FluidStack stack, FluidAction action) {
        if (stack.isEmpty()) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int filled = tank.insert(FluidResource.of(stack), stack.getAmount(), tx);
            if (action.execute()) tx.commit();
            return filled;
        }
    }

    public static FluidStack drain(ResourceHandler<FluidResource> tank, FluidStack stack, FluidAction action) {
        if (stack.isEmpty()) return FluidStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            int drained = tank.extract(FluidResource.of(stack), stack.getAmount(), tx);
            if (action.execute()) tx.commit();
            return stack.copyWithAmount(drained);
        }
    }

    /** Drains whatever the first tank holds. */
    public static FluidStack drain(ResourceHandler<FluidResource> tank, int amount, FluidAction action) {
        FluidResource held = tank.getResource(0);
        return held.isEmpty() ? FluidStack.EMPTY : drain(tank, held.toStack(amount), action);
    }

    public static int fillForced(FluidTankComponent<?> tank, FluidStack stack, FluidAction action) {
        if (stack.isEmpty()) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int filled = tank.insertForced(FluidResource.of(stack), stack.getAmount(), tx);
            if (action.execute()) tx.commit();
            return filled;
        }
    }

    public static FluidStack drainForced(FluidTankComponent<?> tank, int amount, FluidAction action) {
        FluidStack held = tank.getFluid();
        if (held.isEmpty() || amount <= 0) return FluidStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            int drained = tank.extractForced(FluidResource.of(held), amount, tx);
            if (action.execute()) tx.commit();
            return held.copyWithAmount(drained);
        }
    }
}
