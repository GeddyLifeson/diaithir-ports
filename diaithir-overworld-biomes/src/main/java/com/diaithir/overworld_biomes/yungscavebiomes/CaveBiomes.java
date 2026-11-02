package com.diaithir.overworld_biomes.yungscavebiomes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public final class CaveBiomes {
    public static final String MOD_ID = "diaithir_overworld_biomes";
    public static final List<String> NAMES = List.of("crystal_vault", "fungal_hollow", "ember_chasm", "frost_grotto", "rust_caverns", "mycelial_deep");

    private CaveBiomes() {}

    public static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }
}
