package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;

/** Legacy id -> current id remap table. Integrator adds legacy entries here. */
public final class MigrationTable {
    static final Map<Identifier, Identifier> ITEMS = Map.of(
            Identifier.fromNamespaceAndPath("diaithir_overworld_biomes", "compass"),
            Identifier.fromNamespaceAndPath(BiomeCompassMod.MOD_ID, "biome_compass"));

    static void onRegister(RegisterEvent e) {
        if (!e.getRegistryKey().equals(Registries.ITEM)) return;
        // Legacy remapping: register the current item under legacy ids is not supported
        // without MissingMappingsEvent; table is retained for the integrator.
        ITEMS.forEach((legacy, current) -> {
            // no-op: kept for lookup via ITEMS and BuiltInRegistries
            BuiltInRegistries.ITEM.getOptional(current);
        });
    }

    private MigrationTable() {}
}
