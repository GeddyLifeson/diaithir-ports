package net.diaithir.vehicles.fuel;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** Maps fluids to {@link Fuel}s. A fluid is fuel when it is in the {@code diaithir:fuel} fluid tag AND in the tag named by one known fuel id (c:diesel, c:gasoline...). */
public final class Fuels {
    public static final TagKey<Fluid> FUEL_TAG = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("diaithir", "fuel"));

    private Fuels() {}

    public static TagKey<Fluid> kindTag(Fuel f) {
        return TagKey.create(Registries.FLUID, Identifier.parse(f.id()));
    }

    /** The fuel a fluid is, or null if it is not a fuel. */
    public static Fuel of(Fluid fluid) {
        Holder<Fluid> h = fluid.builtInRegistryHolder();
        if (!h.is(FUEL_TAG)) return null;
        for (Fuel f : Fuel.all()) if (h.is(kindTag(f))) return f;
        return null;
    }

    /** A concrete fluid for a fuel (first in its tag), or null if the pack has none. */
    public static Fluid fluidFor(Fuel f) {
        for (Holder<Fluid> h : BuiltInRegistries.FLUID.getTagOrEmpty(kindTag(f))) return h.value();
        return null;
    }
}
