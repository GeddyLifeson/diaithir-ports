package org.diaithir.diesel;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** Fuel table (speed in RPM, strength in SU at that speed, burn in mB per tick), from the upstream default fuel_type data. */
public final class Fuels {
    public record Per(float speed, float strength, float burn) {
        public float capacityPerRpm() {
            return speed == 0 ? 0 : strength / speed;
        }
    }

    public record Fuel(String tag, Per normal, Per modular, Per huge, float soundPitch, float burnerMultiplier) {
        public TagKey<Fluid> fluidTag() {
            return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", tag));
        }
    }

    private static Fuel f(String tag, float pitch, float burner, float[] n, float[] m, float[] h) {
        return new Fuel(tag, new Per(n[0], n[1], .05f), new Per(m[0], m[1], .05f), new Per(h[0], h[1], .05f), pitch, burner);
    }

    public static final List<Fuel> ALL = List.of(
        f("diesel", .9f, .6f, new float[]{96, 6144}, new float[]{96, 8192}, new float[]{224, 16384}),
        f("gasoline", 1f, 1.1f, new float[]{96, 4096}, new float[]{96, 6144}, new float[]{224, 12288}),
        f("biodiesel", 1f, .5f, new float[]{96, 4096}, new float[]{96, 6144}, new float[]{224, 12288}),
        f("ethanol", .7f, .9f, new float[]{96, 2048}, new float[]{96, 3072}, new float[]{192, 6144}),
        f("plantoil", .7f, .5f, new float[]{74, 2048}, new float[]{74, 2560}, new float[]{192, 4096})
    );

    public static @Nullable Fuel of(Fluid fluid) {
        for (Fuel fuel : ALL) {
            if (fluid.builtInRegistryHolder().is(fuel.fluidTag())) return fuel;
        }
        return null;
    }

    private Fuels() {
    }
}
