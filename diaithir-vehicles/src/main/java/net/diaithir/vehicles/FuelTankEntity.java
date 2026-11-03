package net.diaithir.vehicles;

import net.diaithir.vehicles.fuel.FuelFluidView;
import net.diaithir.vehicles.fuel.FuelTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The storage of a fuel tank block: 8,000 L (8 cubic metres) of one fuel. */
public class FuelTankEntity extends BlockEntity {
    public static final long CAPACITY_L = 8000;
    public final FuelTank tank = new FuelTank(CAPACITY_L);
    public final FuelFluidView fluid = new FuelFluidView(tank, this::setChanged);

    public FuelTankEntity(BlockPos pos, BlockState state) {
        super(DiaithirVehicles.FUEL_TANK_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (tank.fuelId() != null) {
            out.putString("fuel", tank.fuelId());
            out.putLong("amount", tank.amountL());
        }
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        tank.load(in.getStringOr("fuel", ""), in.getLongOr("amount", 0));
    }
}
