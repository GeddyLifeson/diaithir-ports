package net.diaithir.units.fuel.station;

import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.FuelStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Pulls fuel from an adjacent tank into every vehicle parked within 5 blocks, {@code litresPerSecond} in total. */
public final class PumpBlockEntity extends BlockEntity {
    public static final int REACH = 5;
    public double lastMoved;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(Station.PUMP_BE.get(), pos, state);
    }

    /** Once a second. Returns the litres moved. */
    public double pump(Level level, double litres) {
        lastMoved = 0;
        TankBlockEntity tank = null;
        for (Direction d : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(d)) instanceof TankBlockEntity t && t.store.quantity() > 0) { tank = t; break; }
        }
        if (tank == null) return 0;
        Fuel f = tank.store.fuel();
        double left = Math.min(litres, tank.store.quantity());
        for (FuelTarget target : Targets.near(level, new AABB(worldPosition).inflate(REACH))) {
            if (left <= 0) break;
            double give = Math.min(left, target.free(f));
            if (give <= 0) continue;
            double took = tank.store.extract(give);
            double put = target.fill(f, took);
            if (put < took) FuelStore.transfer(new Refund(f, took - put), tank.store, took - put);
            left -= put;
            lastMoved += put;
        }
        if (lastMoved > 0) tank.setChanged();
        return lastMoved;
    }

    /** Puts back what a target refused, so no fuel is lost. */
    private static final class Refund implements FuelStore {
        private final Fuel f;
        private double q;

        Refund(Fuel f, double q) { this.f = f; this.q = q; }

        @Override public Fuel fuel() { return q > 0 ? f : null; }
        @Override public double quantity() { return q; }
        @Override public double capacity() { return q; }
        @Override public double insert(Fuel x, double a) { return 0; }
        @Override public double extract(double a) { double t = Math.min(a, q); q -= t; return t; }
    }
}
