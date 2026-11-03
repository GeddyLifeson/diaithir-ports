package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.world.feature.*;
import com.diaithir.overworldbiomes.naturesspirit.world.foliage_placer.*;
import com.diaithir.overworldbiomes.naturesspirit.world.tree_decorator.*;
import com.diaithir.overworldbiomes.naturesspirit.world.trunk.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NSWorldGen {

  public static final String MOD_ID = "natures_spirit";

  public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, MOD_ID);
  public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, MOD_ID);
  public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, MOD_ID);
  public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MOD_ID);

  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<WisteriaTrunkPlacer>> WISTERIA_TRUNK_PLACER = registerTrunkPlacer("wisteria_trunk_placer", WisteriaTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<BushTrunkPlacer>> BUSH_TRUNK_PLACER = registerTrunkPlacer("bush_trunk_placer", BushTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<SugiTrunkPlacer>> SUGI_TRUNK_PLACER = registerTrunkPlacer("sugi_trunk_placer", SugiTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<OliveTrunkPlacer>> OLIVE_TRUNK_PLACER = registerTrunkPlacer("olive_trunk_placer", OliveTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<PaloVerdeTrunkPlacer>> PALO_VERDE_TRUNK_PLACER = registerTrunkPlacer("palo_verde_trunk_placer", PaloVerdeTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<BanyanTrunkPlacer>> BANYAN_TRUNK_PLACER = registerTrunkPlacer("banyan_trunk_placer", BanyanTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<SaxaulTrunkPlacer>> SAXAUL_TRUNK_PLACER = registerTrunkPlacer("saxaul_trunk_placer", SaxaulTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<GhafTrunkPlacer>> GHAF_TRUNK_PLACER = registerTrunkPlacer("ghaf_trunk_placer", GhafTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<MapleTrunkPlacer>> MAPLE_TRUNK_PLACER = registerTrunkPlacer("maple_trunk_placer", MapleTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<MahoganyTrunkPlacer>> MAHOGANY_TRUNK_PLACER = registerTrunkPlacer("mahogany_trunk_placer", MahoganyTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<MegaSugiTrunkPlacer>> MEGA_SUGI_TRUNK_PLACER = registerTrunkPlacer("mega_sugi_trunk_placer", MegaSugiTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<CoconutTrunkPlacer>> COCONUT_TRUNK_PLACER = registerTrunkPlacer("coconut_trunk_placer", CoconutTrunkPlacer.CODEC);
  public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<RedwoodTrunkPlacer>> REDWOOD_TRUNK_PLACER = registerTrunkPlacer("redwood_trunk_placer", RedwoodTrunkPlacer.CODEC);


  public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<WisteriaVinesTreeDecorator>> WISTERIA_VINES_TREE_DECORATOR = registerTreeDecorator("wisteria_vines_tree_decorator",
      WisteriaVinesTreeDecorator.CODEC
  );
  public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<MapleGroundTreeDecorator>> MAPLE_GROUND_TREE_DECORATOR = registerTreeDecorator("maple_ground_tree_decorator",
      MapleGroundTreeDecorator.CODEC);
  public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<CoconutTreeDecorator>> COCONUT_TREE_DECORATOR = registerTreeDecorator("coconut_tree_decorator", CoconutTreeDecorator.CODEC);
  public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<RedwoodBranchTreeDecorator>> REDWOOD_BRANCH_DECORATOR = registerTreeDecorator("redwood_branch_decorator", RedwoodBranchTreeDecorator.CODEC);
  public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<OliveBranchTreeDecorator>> OLIVE_BRANCH_DECORATOR = registerTreeDecorator("olive_branch_decorator", OliveBranchTreeDecorator.CODEC);
  public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<SnowTreeDecorator>> SNOW_DECORATOR = registerTreeDecorator("snow_decorator", SnowTreeDecorator.CODEC);
  public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<PolyporeTreeDecorator>> POLYPORE_DECORATOR = registerTreeDecorator("polypore_decorator", PolyporeTreeDecorator.CODEC);


  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<WisteriaFoliagePlacer>> WISTERIA_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("wisteria_foliage_placer", WisteriaFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<SugiFoliagePlacer>> SUGI_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("sugi_foliage_placer", SugiFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<AspenFoliagePlacer>> ASPEN_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("aspen_foliage_placer", AspenFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<FirFoliagePlacer>> FIR_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("fir_foliage_placer", FirFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<LarchFoliagePlacer>> LARCH_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("larch_foliage_placer", LarchFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<RedwoodFoliagePlacer>> REDWOOD_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("redwood_foliage_placer", RedwoodFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<CypressFoliagePlacer>> CYPRESS_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("cypress_foliage_placer", CypressFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<MapleFoliagePlacer>> MAPLE_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("maple_foliage_placer", MapleFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<CoconutFoliagePlacer>> COCONUT_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("coconut_foliage_placer", CoconutFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<BirchFoliagePlacer>> BIRCH_FOLIAGE_PLACER_TYPE = registerFoliagePlacer("birch_foliage_placer", BirchFoliagePlacer.CODEC);
  public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<GroundedBushFoliagePlacer>> GROUNDED_BUSH_PLACER_TYPE = registerFoliagePlacer("grounded_bush_foliage_placer", GroundedBushFoliagePlacer.CODEC);

  public static final ResourceKey<NormalNoise.NoiseParameters> SUGI_PILLAR = ResourceKey.create(Registries.NOISE, Identifier.fromNamespaceAndPath(MOD_ID, "sugi_pillar"));
  public static final ResourceKey<NormalNoise.NoiseParameters> SUGI_PILLAR_ROOF = ResourceKey.create(Registries.NOISE,
      Identifier.fromNamespaceAndPath(MOD_ID, "sugi_pillar_roof"));
  public static final ResourceKey<NormalNoise.NoiseParameters> SUGI_SURFACE = ResourceKey.create(Registries.NOISE, Identifier.fromNamespaceAndPath(MOD_ID, "sugi_surface"));

  public static final ResourceKey<NormalNoise.NoiseParameters> STRATIFIED_DESERT_PILLAR = ResourceKey.create(Registries.NOISE,
      Identifier.fromNamespaceAndPath(MOD_ID, "stratified_desert_pillar"));
  public static final ResourceKey<NormalNoise.NoiseParameters> STRATIFIED_DESERT_PILLAR_ROOF = ResourceKey.create(Registries.NOISE,
      Identifier.fromNamespaceAndPath(MOD_ID, "stratified_desert_pillar_roof"));
  public static final ResourceKey<NormalNoise.NoiseParameters> STRATIFIED_DESERT_SURFACE = ResourceKey.create(Registries.NOISE,
      Identifier.fromNamespaceAndPath(MOD_ID, "stratified_desert_surface"));

  public static final DeferredHolder<Feature<?>, Feature<DeltaFeatureConfiguration>> HIBISCUS_DELTA_FEATURE = FEATURES.register("water_delta_feature",
      () -> new NSDeltaFeature(DeltaFeatureConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<OreConfiguration>> HIBISCUS_PUMPKIN_PATCH_FEATURE = FEATURES.register("pumpkin_patch_feature",
      () -> new PumpkinPatchFeature(OreConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<BlockPileConfiguration>> HIBISCUS_LARGE_PUMPKIN_FEATURE = FEATURES.register("large_pumpkin_feature",
      () -> new LargePumpkinFeature(BlockPileConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<TurnipRootFeatureConfig>> HIBISCUS_TURNIP_ROOT_FEATURE = FEATURES.register("turnip_root_feature",
      () -> new TurnipRootFeature(TurnipRootFeatureConfig.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> JOSHUA_TREE_FEATURE = FEATURES.register("joshua_tree_feature",
      () -> new JoshuaTreeFeature(NoneFeatureConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ALLUAUDIA_FEATURE = FEATURES.register("alluaudia_feature",
      () -> new AlluaudiaFeature(NoneFeatureConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration>> HUGE_SHIITAKE_MUSHROOM_FEATURE = FEATURES.register("huge_shiitake_mushroom_feature",
      () -> new HugeShiitakeMushroomFeature(net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<HugeMushroomFeatureConfig>> HUGE_RED_MUSHROOM_FEATURE = FEATURES.register("huge_red_mushroom_feature",
      () -> new HugeRedMushroomFeature(HugeMushroomFeatureConfig.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<HugeMushroomFeatureConfig>> HUGE_BROWN_MUSHROOM_FEATURE = FEATURES.register("huge_brown_mushroom_feature",
      () -> new HugeBrownMushroomFeature(HugeMushroomFeatureConfig.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> POLYPORE_FEATURE = FEATURES.register("polypore_feature",
      () -> new PolyporeFeature(NoneFeatureConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> LOTUS_PLANT_FEATURE = FEATURES.register("lotus_plant_feature",
      () -> new LotusPlantFeature(NoneFeatureConfiguration.CODEC)
  );
  public static final DeferredHolder<Feature<?>, Feature<RandomPatchConfiguration>> LEVELED_RANDOM_PATCH_FEATURE = FEATURES.register("leveled_random_patch_feature",
      () -> new LeveledRandomPatch(RandomPatchConfiguration.CODEC)
  );

  private static <P extends FoliagePlacer> DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<P>> registerFoliagePlacer(String id, MapCodec<P> codec) {
    return FOLIAGE_PLACERS.register(id, () -> new FoliagePlacerType<>(codec));
  }

  private static <P extends TrunkPlacer> DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<P>> registerTrunkPlacer(String id, MapCodec<P> codec) {
    return TRUNK_PLACERS.register(id, () -> new TrunkPlacerType<>(codec));
  }

  private static <P extends TreeDecorator> DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<P>> registerTreeDecorator(String id, MapCodec<P> codec) {
    return TREE_DECORATORS.register(id, () -> new TreeDecoratorType<>(codec));
  }

  public static void init(IEventBus modBus) {
    TRUNK_PLACERS.register(modBus);
    TREE_DECORATORS.register(modBus);
    FOLIAGE_PLACERS.register(modBus);
    FEATURES.register(modBus);
  }

  public static void registerWorldGen() {
    // Biome feature injection is handled through data-driven biome modifiers in this port.
  }

}
