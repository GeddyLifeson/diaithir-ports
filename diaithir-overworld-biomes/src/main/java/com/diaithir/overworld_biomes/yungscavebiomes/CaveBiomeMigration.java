package com.diaithir.overworld_biomes.yungscavebiomes;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.Map;

public final class CaveBiomeMigration {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String LEGACY_NS = "yungs_cave_biomes";
    private static final Map<String, String> BIOME_ALIASES = Map.ofEntries(
            Map.entry("crystal_caves", "crystal_vault"),
            Map.entry("crystal_cave", "crystal_vault"),
            Map.entry("mushroom_caves", "fungal_hollow"),
            Map.entry("fungal_caves", "fungal_hollow"),
            Map.entry("lava_caves", "ember_chasm"),
            Map.entry("magma_caves", "ember_chasm"),
            Map.entry("ice_caves", "frost_grotto"),
            Map.entry("frozen_caves", "frost_grotto"),
            Map.entry("copper_caves", "rust_caverns"),
            Map.entry("rusty_caves", "rust_caverns"),
            Map.entry("deep_caves", "mycelial_deep"),
            Map.entry("dark_caves", "mycelial_deep"));

    private CaveBiomeMigration() {}

    /** Best-effort, unverified remap of a legacy biome id; returns null when unknown. */
    public static Identifier remapBiome(Identifier legacy) {
        if (!LEGACY_NS.equals(legacy.getNamespace())) return null;
        String target = BIOME_ALIASES.get(legacy.getPath());
        if (target == null) {
            LOG.warn("Unknown legacy cave biome {}, falling back to minecraft:dripstone_caves", legacy);
            return Identifier.withDefaultNamespace("dripstone_caves");
        }
        return Identifier.fromNamespaceAndPath(CaveBiomes.MOD_ID, target);
    }

    /** Blocks/items of the legacy mod are not reproduced; log and fall back. */
    public static Identifier remapBlock(Identifier legacy) {
        if (!LEGACY_NS.equals(legacy.getNamespace())) return null;
        LOG.warn("Missing legacy block {} replaced by minecraft:stone", legacy);
        return Identifier.withDefaultNamespace("stone");
    }
}
