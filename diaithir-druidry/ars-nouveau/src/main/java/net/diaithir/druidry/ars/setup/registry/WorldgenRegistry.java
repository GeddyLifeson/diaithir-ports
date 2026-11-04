package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Trimmed port: only the worldgen keys. Feature/bootstrap code removed (needs LightFeature, MagicTrunkPlacer); worldgen should be datapack JSON. */
public class WorldgenRegistry {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_CASCADING_TREE = registerConfKey("cascading_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_BLAZING_TREE = registerConfKey("blazing_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_VEXING_TREE = registerConfKey("vexing_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_FLOURISHING_TREE = registerConfKey("flourishing_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATURAL_CONFIGURED_CASCADING_TREE = registerConfKey("natural_cascading_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATURAL_CONFIGURED_BLAZING_TREE = registerConfKey("natural_blazing_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATURAL_CONFIGURED_VEXING_TREE = registerConfKey("natural_vexing_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATURAL_CONFIGURED_FLOURISHING_TREE = registerConfKey("natural_flourishing_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BERRY_BUSH = registerConfKey("patch_berry");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_LIGHTS = registerConfKey("lights");
    public static final ResourceKey<PlacedFeature> PLACED_NATURAL_CASCADING_TREE = registerPlacedKey("placed_cascading_tree");
    public static final ResourceKey<PlacedFeature> PLACED_NATURAL_BLAZING_TREE = registerPlacedKey("placed_blazing_tree");
    public static final ResourceKey<PlacedFeature> PLACED_NATURAL_VEXING_TREE = registerPlacedKey("placed_vexing_tree");
    public static final ResourceKey<PlacedFeature> PLACED_NATURAL_FLOURISHING_TREE = registerPlacedKey("placed_flourishing_tree");
    public static final ResourceKey<PlacedFeature> PLACED_BERRY_BUSH = registerPlacedKey("placed_berry_bush");
    public static final ResourceKey<PlacedFeature> PLACED_MIX_ARCHWOODS = registerPlacedKey("placed_mixed_archwoods");
    public static final ResourceKey<PlacedFeature> PLACED_DENSE_ARCHWOODS = registerPlacedKey("placed_dense_archwoods");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MIXED_ARCHWOODS = registerConfKey("mixed_archwoods");
    public static final ResourceKey<PlacedFeature> PLACED_MOJANK_GRASS = registerPlacedKey("mojang_grass");
    public static final ResourceKey<PlacedFeature> PLACED_MOJANK_FLOWERS = registerPlacedKey("mojang_flowers");
    public static final ResourceKey<PlacedFeature> PLACED_LIGHTS = registerPlacedKey("placed_lights");

    public static ResourceKey<ConfiguredFeature<?, ?>> registerConfKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ArsNouveau.prefix(name));
    }

    public static ResourceKey<PlacedFeature> registerPlacedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ArsNouveau.prefix(name));
    }
}
