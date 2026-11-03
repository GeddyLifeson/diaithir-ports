package net.diaithir.vehicles;

import net.diaithir.bridges.Ports;
import net.diaithir.units.fuel.FuelStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Finds a fuel store: on a block through {@code Ports.FUEL} (the Carrier Tank and every bridge block), on an entity through {@link FuelHolder}. */
public final class FuelLookup {
    private FuelLookup() {}

    public static FuelStore block(Level level, BlockPos pos, Direction side) {
        return level.getCapability(Ports.FUEL, pos, side);
    }

    public static FuelStore entity(Entity e) {
        return e instanceof FuelHolder h ? h.fuelStore() : null;
    }
}
