package net.diaithir.steamrails;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Water-only buffer that Create's pipes, tanks and spouts see. Create counts 81000 units per bucket;
 * the sims count millibuckets, so {@link #drain} converts.
 */
public final class SteamIntake implements FluidInventory {
    public static final int UNITS_PER_MB = 81;
    public static final int CAPACITY = 4000 * UNITS_PER_MB;

    private FluidStack stack = FluidStack.EMPTY;
    private final Runnable onChange;

    public SteamIntake(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    public FluidStack getStack(int slot) {
        return stack;
    }

    @Override
    public void setStack(int slot, FluidStack s) {
        stack = s.isEmpty() ? FluidStack.EMPTY : (s.getAmount() > CAPACITY ? s.directCopy(CAPACITY) : s);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int getMaxAmountPerStack() {
        return CAPACITY;
    }

    @Override
    public boolean isValid(int slot, FluidStack s) {
        return s.isOf(Fluids.WATER);
    }

    @Override
    public void markDirty() {
        onChange.run();
    }

    /** Millibuckets waiting in the buffer. */
    public int millibuckets() {
        return stack.isEmpty() ? 0 : stack.getAmount() / UNITS_PER_MB;
    }

    /** Removes {@code mb} millibuckets that the sim accepted. */
    public void take(int mb) {
        if (mb <= 0 || stack.isEmpty()) return;
        int left = stack.getAmount() - mb * UNITS_PER_MB;
        stack = left <= 0 ? FluidStack.EMPTY : stack.directCopy(left);
    }

    public void save(ValueOutput out) {
        out.putInt("units", stack.isEmpty() ? 0 : stack.getAmount());
    }

    public void load(ValueInput in) {
        int u = in.getIntOr("units", 0);
        stack = u > 0 ? new FluidStack(Fluids.WATER, u) : FluidStack.EMPTY;
    }
}
