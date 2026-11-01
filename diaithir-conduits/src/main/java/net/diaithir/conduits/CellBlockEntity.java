package net.diaithir.conduits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.jspecify.annotations.Nullable;

/** Holds the tank of a fluid cell or the FE of an energy cell. */
public final class CellBlockEntity extends BlockEntity {
    public final class Tank extends FluidStacksResourceHandler {
        Tank() {
            super(1, Config.FLUID_CELL_CAPACITY.get());
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previous) {
            setChanged();
        }
    }

    public final class Battery extends SimpleEnergyHandler {
        Battery() {
            super(Config.ENERGY_CELL_CAPACITY.get(), Config.CELL_TRANSFER.get(), Config.CELL_TRANSFER.get());
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }

    public final @Nullable Tank tank;
    public final @Nullable Battery battery;

    public CellBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirConduits.CELL_BE.get(), pos, state);
        boolean fluid = ((CellBlock) state.getBlock()).kind == 0;
        tank = fluid ? new Tank() : null;
        battery = fluid ? null : new Battery();
    }

    public String describe() {
        if (tank != null) {
            FluidResource r = tank.getResource(0);
            return "Fluid cell: " + (r.isEmpty() ? "empty" : BuiltInRegistries.FLUID.getKey(r.getFluid()) + " " + tank.getAmountAsInt(0)) + " / " + Config.FLUID_CELL_CAPACITY.get() + " mB";
        }
        return "Energy cell: " + battery.getAmountAsInt() + " / " + Config.ENERGY_CELL_CAPACITY.get() + " FE";
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (tank != null) tank.serialize(out.child("tank"));
        if (battery != null) battery.serialize(out.child("fe"));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        if (tank != null) in.child("tank").ifPresent(tank::deserialize);
        if (battery != null) in.child("fe").ifPresent(battery::deserialize);
    }
}
