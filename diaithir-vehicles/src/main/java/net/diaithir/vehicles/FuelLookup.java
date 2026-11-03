package net.diaithir.vehicles;

import net.diaithir.vehicles.fuel.FluidFuelStore;
import net.diaithir.vehicles.fuel.FuelCaps;
import net.diaithir.vehicles.fuel.FuelStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Finds a fuel store on a block or entity: the shared fuel capability first, then any other mod's fluid handler (fuel fluids only). */
public final class FuelLookup {
    private FuelLookup() {}

    public static FuelStore block(Level level, BlockPos pos, Direction side) {
        FuelStore s = level.getCapability(FuelCaps.BLOCK, pos, side);
        if (s != null) return s;
        var h = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return h == null ? null : new FluidFuelStore(h);
    }

    public static FuelStore entity(Entity e) {
        FuelStore s = e.getCapability(FuelCaps.ENTITY, null);
        if (s != null) return s;
        var h = e.getCapability(Capabilities.Fluid.ENTITY, null);
        return h == null ? null : new FluidFuelStore(h);
    }
}
