package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

public final class GeneratorBlockEntity extends BlockEntity {
    public static final int MAX_OUT = 1024, MAX_CANISTERS = 4;

    public final FeBuffer energy = new FeBuffer(4000, 0, MAX_OUT, this::setChanged);
    private int fuelLeft;
    private int rate;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.GENERATOR_BE.get(), pos, state);
    }

    public @Nullable EnergyHandler energyFor(@Nullable Direction side) {
        return energy;
    }

    public int fuelLeft() {
        return fuelLeft;
    }

    /** Adds one canister if there is room (up to 4 of the same fuel). Returns false if refused. */
    public boolean refuel(ItemStack stack) {
        if (!(stack.getItem() instanceof FuelItem f)) return false;
        if (fuelLeft > 0 && rate != f.fePerTick) return false;
        if (fuelLeft + f.ticks > f.ticks * MAX_CANISTERS) return false;
        fuelLeft += f.ticks;
        rate = f.fePerTick;
        setChanged();
        return true;
    }

    public void serverTick(ServerLevel level) {
        if (fuelLeft > 0 && energy.room() >= rate) {
            energy.give(rate);
            fuelLeft--;
            if (fuelLeft == 0) setChanged();
        }
        if (energy.amount() > 0) energy.pushToNeighbours(level, worldPosition, MAX_OUT, false);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        energy.serialize(out.child("fe"));
        out.putInt("fuel", fuelLeft);
        out.putInt("rate", rate);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("fe").ifPresent(energy::deserialize);
        fuelLeft = in.getIntOr("fuel", 0);
        rate = in.getIntOr("rate", 0);
    }
}
