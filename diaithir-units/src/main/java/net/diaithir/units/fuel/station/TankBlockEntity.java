package net.diaithir.units.fuel.station;

import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.Fuels;
import net.diaithir.units.fuel.SimpleStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A storage tank holding one fuel; saved with the block. */
public final class TankBlockEntity extends BlockEntity {
    public final SimpleStore store;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(Station.TANK_BE.get(), pos, state);
        store = new SimpleStore(((TankBlock) state.getBlock()).capacityL);
    }

    public FuelStore store() {
        return store;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        Fuel f = store.fuel();
        out.putString("Fuel", f == null ? "" : f.id());
        out.putDouble("Litres", store.quantity());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        store.extract(store.quantity());
        Fuel f = Fuels.get(in.getStringOr("Fuel", ""));
        if (f != null) store.insert(f, in.getDoubleOr("Litres", 0));
    }
}
