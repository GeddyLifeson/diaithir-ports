package net.diaithir.bridges;

import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.Fuels;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * The shared ports every working block in the pack plugs into: the fuel carrier capability (bridge F), fluid mapping for liquid fuels (bridge L)
 * and the comparator signal (bridge S). Other modules read {@link #FUEL} to find a tank, pump or engine feed on a face.
 */
public final class Ports {
    private Ports() {}

    /** A block's fuel store on a face (litres for liquids, kg for solids and batteries, quanta for magic). Engines match it with {@code Engine.accepts}. */
    public static final BlockCapability<FuelStore, Direction> FUEL =
            BlockCapability.createSided(Identifier.parse("diaithirbridges:fuel_store"), FuelStore.class);

    /** Fluid tag a liquid fuel travels as: {@code c:<fuel id>} (for example {@code c:diesel}). 1 mB = 1 L. */
    public static TagKey<Fluid> fluidTag(Fuel f) {
        return TagKey.create(Registries.FLUID, Identifier.parse("c:" + f.id()));
    }

    /** The liquid fuel a fluid stands for, or null. */
    public static Fuel fuelOf(FluidResource r) {
        if (r.isEmpty()) return null;
        for (Fuel f : Fuels.all()) if (f.kind() == Fuel.Kind.LIQUID && r.typeHolder().is(fluidTag(f))) return f;
        return null;
    }

    /** The first fluid registered under a liquid fuel's tag, or EMPTY when no mod supplies one. */
    public static FluidResource fluidOf(Fuel f) {
        if (f == null || f.kind() != Fuel.Kind.LIQUID) return FluidResource.EMPTY;
        for (var holder : BuiltInRegistries.FLUID.getTagOrEmpty(fluidTag(f))) return FluidResource.of(holder);
        return FluidResource.EMPTY;
    }

    /** Redstone comparator level 0..15 for a fill fraction: 0 only when empty, 15 only when full. */
    public static int level(double fraction) {
        if (!(fraction > 0)) return 0;
        if (fraction >= 1) return 15;
        return 1 + (int) (fraction * 14);
    }
}
