package com.github.alexmodguy.alexscaves.server.level.structure;

import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Parent bowl placement per cave biome. The numbers must match data/alexscaves/worldgen/structure_set/<parent>.json.
 */
public final class ACCaveParents {
    private ACCaveParents() {
    }

    public static final CaveMaskBiomeSource.Parent CANDY = new CaveMaskBiomeSource.Parent(31, 8, 103, 100, -48, 100, false);
    public static final CaveMaskBiomeSource.Parent PRIMORDIAL = new CaveMaskBiomeSource.Parent(37, 8, 101, 100, -1, 100, false);
    public static final CaveMaskBiomeSource.Parent FORLORN = new CaveMaskBiomeSource.Parent(41, 8, 106, 100, -10, 100, false);
    public static final CaveMaskBiomeSource.Parent TOXIC = new CaveMaskBiomeSource.Parent(43, 8, 102, 90, 4, 90, false);
    public static final CaveMaskBiomeSource.Parent MAGNETIC = new CaveMaskBiomeSource.Parent(47, 8, 104, 100, 20, 80, false);
    public static final CaveMaskBiomeSource.Parent ABYSSAL = new CaveMaskBiomeSource.Parent(53, 8, 105, 150, -16, 150, true);

    public static CaveMaskBiomeSource.Parent forBiome(ResourceKey<Biome> key) {
        if (key.equals(ACBiomeRegistry.CANDY_CAVITY)) return CANDY;
        if (key.equals(ACBiomeRegistry.PRIMORDIAL_CAVES)) return PRIMORDIAL;
        if (key.equals(ACBiomeRegistry.FORLORN_HOLLOWS)) return FORLORN;
        if (key.equals(ACBiomeRegistry.TOXIC_CAVES)) return TOXIC;
        if (key.equals(ACBiomeRegistry.MAGNETIC_CAVES)) return MAGNETIC;
        if (key.equals(ACBiomeRegistry.ABYSSAL_CHASM)) return ABYSSAL;
        return null;
    }
}
