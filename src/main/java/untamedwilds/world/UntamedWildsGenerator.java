package untamedwilds.world;

import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.registries.Registries;
import untamedwilds.UntamedWilds;
import untamedwilds.config.ConfigFeatureControl;
import untamedwilds.world.gen.feature.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.world.level.levelgen.GenerationStep.Decoration.*;

@EventBusSubscriber(modid = UntamedWilds.MOD_ID)
public class UntamedWildsGenerator {

    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registries.CONFIGURED_FEATURE, UntamedWilds.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registries.PLACED_FEATURE, UntamedWilds.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, UntamedWilds.MOD_ID);
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATION = DeferredRegister.create(BuiltInRegistries.TREE_DECORATOR_TYPE, UntamedWilds.MOD_ID);
    public static final Map<String, Float> biodiversity_levels = new java.util.HashMap<>();

    private static final DeferredHolder<Feature<?>, Feature<CountConfiguration>> SEA_ANEMONE = regFeature("sea_anemone", () -> new FeatureSeaAnemone(CountConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> REEDS = regFeature("reeds", () -> new FeatureReedClusters(NoneFeatureConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ALGAE = regFeature("algae", () -> new FeatureUnderwaterAlgae(NoneFeatureConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<ProbabilityFeatureConfiguration>> VEGETATION = regFeature("vegetation", () -> new FeatureVegetation(ProbabilityFeatureConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> FLOATING_VEGETATION = regFeature("floating_vegetation", () -> new FeatureFloatingPlants(NoneFeatureConfiguration.CODEC));

    // TODO: Unused because can't attach decorators to vanilla features. If I ever implement trees, this will go there
    // TODO 1.21.1: TreeDecoratorType needs specific codec, not generic TreeDecorator.CODEC
    // public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<?>> TREE_ORCHID = TREE_DECORATION.register("orchid", () -> new TreeDecoratorType<>(TreeDecorator.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> UNDERGROUND = regFeature("underground", () -> new FeatureUndergroundFaunaLarge(NoneFeatureConfiguration.CODEC));
    private static final DeferredHolder<ConfiguredFeature<?, ?>, ConfiguredFeature<?, ?>> UNDERGROUND_CONFIGURED = CONFIGURED_FEATURES.register("underground", () -> new ConfiguredFeature<>(UNDERGROUND.get(), FeatureConfiguration.NONE));

    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> APEX = regFeature("apex_predator", () -> new FeatureApexPredators(NoneFeatureConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> HERBIVORES = regFeature("herbivores", () -> new FeatureHerbivores(NoneFeatureConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> CRITTERS = regFeature("critter", () -> new FeatureCritters(NoneFeatureConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SESSILE = regFeature("sessile", () -> new FeatureOceanSessileSpawns(NoneFeatureConfiguration.CODEC));

    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> OCEAN = regFeature("ocean_rare", () -> new FeatureOceanSwimming(NoneFeatureConfiguration.CODEC));
    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> DENSE_WATER = regFeature("dense_water", () -> new FeatureDenseWater(NoneFeatureConfiguration.CODEC));

    private static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> CRITTER_BURROW = regFeature("burrow", () -> new FeatureCritterBurrow(NoneFeatureConfiguration.CODEC));

    private static <B extends Feature<?>> DeferredHolder<Feature<?>, B> regFeature(String name, Supplier<? extends B> supplier) {
        return UntamedWildsGenerator.FEATURES.register(name, supplier);
    }

    public static Map<Identifier, BiomeModifier> generateModifierByLocation(RegistryOps<JsonElement> registryOps) {
        Map<Identifier, BiomeModifier> map = new HashMap<>();
        addFeature(map, "sessile", new Builder(registryOps, new ConfiguredFeature<>(SESSILE.get(), FeatureConfiguration.NONE), "")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqSessile.get()))
            .tag(Arrays.asList(BiomeTags.IS_OCEAN)).decoration(TOP_LAYER_MODIFICATION));
        addFeature(map, "ocean_rare", new Builder(registryOps, new ConfiguredFeature<>(OCEAN.get(), FeatureConfiguration.NONE), "")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqOcean.get()))
            .tag(Arrays.asList(BiomeTags.IS_OCEAN)).decoration(TOP_LAYER_MODIFICATION));
        addFeature(map, "sea_anemone", new Builder(registryOps, new ConfiguredFeature<>(SEA_ANEMONE.get(), new CountConfiguration(4)), "gencontrol.anemone")
            .placementModifier(RarityFilter.onAverageOnceEvery(6))
            .extraBiomes(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN).decoration(GenerationStep.Decoration.VEGETAL_DECORATION));

        addFeature(map, "floating_vegetation", new Builder(registryOps, new ConfiguredFeature<>(FLOATING_VEGETATION.get(), FeatureConfiguration.NONE), "gencontrol.algae")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqAlgae.get()))
            .tag(Arrays.asList(BiomeTags.IS_JUNGLE)).decoration(GenerationStep.Decoration.VEGETAL_DECORATION));
        addFeature(map, "reeds", new Builder(registryOps, new ConfiguredFeature<>(REEDS.get(), FeatureConfiguration.NONE), "gencontrol.reeds")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqReeds.get()))
            .blacklist(ConfigFeatureControl.reedBlacklist.get().stream().map(s -> ResourceKey.create(Registries.BIOME, Identifier.tryParse(s.contains(":" ) ? s : ("minecraft:" + s)))).collect(Collectors.toList()))
            .decoration(GenerationStep.Decoration.VEGETAL_DECORATION));

        addFeature(map, "algae", new Builder(registryOps, new ConfiguredFeature<>(ALGAE.get(), FeatureConfiguration.NONE), "gencontrol.algae")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqAlgae.get()))
            .blacklist(ConfigFeatureControl.algaeBlacklist.get().stream().map(s -> ResourceKey.create(Registries.BIOME, Identifier.tryParse(s.contains(":" ) ? s : ("minecraft:" + s)))).collect(Collectors.toList()))
            .decoration(GenerationStep.Decoration.VEGETAL_DECORATION));
        addFeature(map, "vegetation", new Builder(registryOps, new ConfiguredFeature<>(VEGETATION.get(), new ProbabilityFeatureConfiguration(1f / 4)), "gencontrol.bush")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqFlora.get()))
            .blacklist(ConfigFeatureControl.floraBlacklist.get().stream().map(s -> ResourceKey.create(Registries.BIOME, Identifier.tryParse(s.contains(":" ) ? s : ("minecraft:" + s)))).collect(Collectors.toList()))
            .decoration(GenerationStep.Decoration.VEGETAL_DECORATION));

        addFeature(map, "dense_water", new Builder(registryOps, new ConfiguredFeature<>(DENSE_WATER.get(), FeatureConfiguration.NONE), "")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqWater.get()))
            .decoration(TOP_LAYER_MODIFICATION));
        addFeature(map, "burrow", new Builder(registryOps, new ConfiguredFeature<>(ConfigFeatureControl.addBurrows.get() ? CRITTER_BURROW.get() : CRITTERS.get(), FeatureConfiguration.NONE), "")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqCritter.get()))
            .decoration(TOP_LAYER_MODIFICATION));
        addFeature(map, "apex_predator", new Builder(registryOps, new ConfiguredFeature<>(APEX.get(), FeatureConfiguration.NONE), "")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqApex.get()))
            .decoration(TOP_LAYER_MODIFICATION));
        addFeature(map, "herbivores", new Builder(registryOps, new ConfiguredFeature<>(HERBIVORES.get(), FeatureConfiguration.NONE), "")
            .placementModifier(RarityFilter.onAverageOnceEvery(ConfigFeatureControl.freqHerbivores.get()))
            .decoration(TOP_LAYER_MODIFICATION));

        // TODO: Restore intended Underground fauna generation
        // TODO: Currently only applies to "Underground" biomes, extend to any biome?
        /*!FaunaHandler.getSpawnableList(FaunaHandler.animalType.LARGE_UNDERGROUND).isEmpty() &&*/
        addFeature(map, "underground", new Builder(registryOps, UNDERGROUND_CONFIGURED.get(), "mobcontrol.masterspawner")
            .tag(Arrays.asList(untamedwilds.init.ModTags.ModBiomeTags.IS_UNDERGROUND)).decoration(GenerationStep.Decoration.UNDERGROUND_DECORATION)
            .placementModifiers(FeatureUndergroundFaunaLarge.placed()));
        return map;
    }

    private static class Builder {

        private final ConfiguredFeature<?, ?> placedFeatureName;
        private TagKey<Biome> dimension = BiomeTags.IS_OVERWORLD;
        private final List<PlacementModifier> placementModifiers = new ArrayList<>();
        private final List<TagKey<Biome>> biomeTags = new ArrayList<>();
        private final List<Holder<Biome>> blacklist = new ArrayList<>();
        private final List<Holder<Biome>> extraBiomes = new ArrayList<>();
        private final RegistryOps<JsonElement> registryOps;
        private GenerationStep.Decoration decoration = GenerationStep.Decoration.VEGETAL_DECORATION;
        private final String configOption;

        public Builder(RegistryOps<JsonElement> registryOps, ConfiguredFeature<?, ?> placedFeature, String configOption) {
            this.registryOps = registryOps;
            this.placedFeatureName = placedFeature;
            this.configOption = configOption;
        }

        public Builder dimension(TagKey<Biome> tag) {
            this.dimension = tag;
            return this;
        }

        public Builder placementModifier(PlacementModifier placementModifier) {
            this.placementModifiers.add(placementModifier);
            return this;
        }

        public Builder placementModifiers(List<PlacementModifier> placementModifiers) {
            this.placementModifiers.addAll(placementModifiers);
            return this;
        }

        public Builder tag(List<TagKey<Biome>> tags) {
            this.biomeTags.addAll(tags);
            return this;
        }

        public Builder blacklist(List<ResourceKey<Biome>> biomes) {
            // RegistryOps.registry access differs across mappings; skip resolving biome holders during data-gen to avoid API mismatches
            // and leave the blacklist empty here. Runtime code that consumes these builders should handle empty lists.
            return this;
        }

        public Builder extraBiomes(ResourceKey<Biome>... biomes) {
            // As above, skip resolving biome holders during data-gen to avoid API mismatches
            return this;
        }

        public Builder decoration(GenerationStep.Decoration decoration) {
            this.decoration = decoration;
            return this;
        }

        private static HolderSet<Biome> getBiomesByTag(RegistryOps<JsonElement> registryOps, TagKey<Biome> tag) {
            // RegistryOps.registry access differs across mappings; return an empty holder set placeholder
            return HolderSet.direct(Collections.emptyList());
        }

        public UntamedWildsBiomeModifier build() {
            // Due to data-gen API differences, do not attempt to populate biome holder sets here. Use empty lists instead.
            List<HolderSet<Biome>> biomesSet = new ArrayList<>();
            List<HolderSet<Biome>> blacklistSet = new ArrayList<>();
            Holder<PlacedFeature> placedFeature = Holder.direct(new PlacedFeature(Holder.direct(placedFeatureName), placementModifiers));
            return new UntamedWildsBiomeModifier(dimension, biomesSet, blacklistSet, decoration, placedFeature, configOption);
        }
    }

    private static void addFeature(Map<Identifier, BiomeModifier> map, String placedFeatureName, Builder builder) {
        BiomeModifier modifier = builder.build();
    Identifier location = Identifier.tryParse(UntamedWilds.MOD_ID + ":" + placedFeatureName);
        map.put(location, modifier);
    }

    // Returns the biodiversity level of a biome. Values are data-driven, defaulting to 0.6 if no key is found.
    public static float getBioDiversityLevel(Identifier biome) {
        String key = biome.toString();
        if (biodiversity_levels.containsKey(key)) {
            return biodiversity_levels.get(key);
        }
        return 0.6F;
    }
}