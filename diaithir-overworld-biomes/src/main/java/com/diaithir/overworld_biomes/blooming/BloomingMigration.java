package com.diaithir.overworld_biomes.blooming;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class BloomingMigration {
    /** Legacy id table: bloomingbiosphere:<name> -> diaithir_overworld_biomes:<name>. Worldgen ids are not persisted; blocks/items only. */
    public static final String LEGACY_NAMESPACE = "bloomingbiosphere";
    public static final Map<String, String> LEGACY_MAP = Map.of();

    private BloomingMigration() {}

    public static void registerMigration(RegisterEvent event) {
        // No custom blocks/items registered: nothing to alias. Table kept for developers to populate (see legacy_id_map.json).
    }

    public static Identifier remap(Identifier old) {
        String target = LEGACY_MAP.get(old.getPath());
        return LEGACY_NAMESPACE.equals(old.getNamespace()) && target != null
                ? Identifier.fromNamespaceAndPath(BloomingBiosphere.MOD_ID, target) : old;
    }
}
