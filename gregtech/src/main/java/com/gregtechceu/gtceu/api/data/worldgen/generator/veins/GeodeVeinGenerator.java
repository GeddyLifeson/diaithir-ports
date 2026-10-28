package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import net.minecraft.util.valueproviders.IntProviders;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Accessors(chain = true, fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class GeodeVeinGenerator extends VeinGenerator {

    public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0, 1.0);
    public static final Codec<IntProvider> SIZE_PROVIDER = IntProviders.codec(1, 20);
    // spotless:off
    public static final MapCodec<GeodeVeinGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                    GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter((config) -> config.geodeBlockSettings),
                    GeodeLayerSettings.CODEC.fieldOf("layers").forGetter((config) -> config.geodeLayerSettings),
                    GeodeCrackSettings.CODEC.fieldOf("crack").forGetter((config) -> config.geodeCrackSettings),
                    CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse(0.35).forGetter((config) -> config.usePotentialPlacementsChance),
                    CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0).forGetter((config) -> config.useAlternateLayer0Chance),
                    Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse(true).forGetter((config) -> config.placementsRequireLayer0Alternate),
                    SIZE_PROVIDER.fieldOf("outer_wall_distance").orElse(UniformInt.of(4, 5)).forGetter((config) -> config.outerWallDistance),
                    SIZE_PROVIDER.fieldOf("distribution_points").orElse(UniformInt.of(3, 4)).forGetter((config) -> config.distributionPoints),
                    SIZE_PROVIDER.fieldOf("point_offset").orElse(UniformInt.of(1, 2)).forGetter((config) -> config.pointOffset),
                    Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter((config) -> config.minGenOffset),
                    Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter((config) -> config.maxGenOffset),
                    CHANCE_RANGE.fieldOf("noise_multiplier").orElse(0.05).forGetter((config) -> config.noiseMultiplier),
                    Codec.INT.fieldOf("invalid_blocks_threshold").forGetter((config) -> config.invalidBlocksThreshold)
    ).apply(instance, GeodeVeinGenerator::new));
    // spotless:on
    @Setter
    public GeodeBlockSettings geodeBlockSettings;
    @Setter
    public GeodeLayerSettings geodeLayerSettings;
    @Setter
    public GeodeCrackSettings geodeCrackSettings;
    @Setter
    public double usePotentialPlacementsChance = 0.5;
    @Setter
    public double useAlternateLayer0Chance = 0.0;
    @Setter
    public boolean placementsRequireLayer0Alternate = false;
    @Setter
    public IntProvider outerWallDistance = ConstantInt.of(0);
    @Setter
    public IntProvider distributionPoints = ConstantInt.of(0);
    @Setter
    public IntProvider pointOffset = ConstantInt.of(0);
    @Setter
    public int minGenOffset = 0;
    @Setter
    public int maxGenOffset = 0;
    @Setter
    public double noiseMultiplier = 1.0;
    @Setter
    public int invalidBlocksThreshold = 0;

    @Override
    public List<VeinEntry> getAllEntries() { return null; /* ported stub */ }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) { return null; /* ported stub */ }

    protected void safeSetBlock(BulkSectionAccess level, LevelChunkSection section, BlockPos pos, BlockState state,
                                Predicate<BlockState> oldState) {
        if (oldState.test(level.getBlockState(pos))) {
            int x = SectionPos.sectionRelative(pos.getX());
            int y = SectionPos.sectionRelative(pos.getY());
            int z = SectionPos.sectionRelative(pos.getZ());
            section.setBlockState(x, y, z, state, false);
        }
    }

    protected BlockState getStateFromEither(Either<BlockStateProvider, Material> either, GeodeBlockSettings settings,
                                            RandomSource random, BlockPos pos) { return null; /* ported stub */ }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new GeodeVeinGenerator(this.geodeBlockSettings, this.geodeLayerSettings, this.geodeCrackSettings,
                this.usePotentialPlacementsChance, this.useAlternateLayer0Chance, this.placementsRequireLayer0Alternate,
                this.outerWallDistance, this.distributionPoints, this.pointOffset, this.minGenOffset, this.maxGenOffset,
                this.noiseMultiplier, this.invalidBlocksThreshold);
    }

    @Override
    public MapCodec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public record GeodeBlockSettings(Either<BlockStateProvider, Material> fillingProvider,
                                     Either<BlockStateProvider, Material> innerLayerProvider,
                                     Either<BlockStateProvider, Material> alternateInnerLayerProvider,
                                     Either<BlockStateProvider, Material> middleLayerProvider,
                                     Either<BlockStateProvider, Material> outerLayerProvider,
                                     List<BlockState> innerPlacements, TagKey<Block> cannotReplace,
                                     TagKey<Block> invalidBlocks, @NotNull TagPrefix providerMaterialPrefix) {

        // spotless:off
        public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.byNameCodec()).fieldOf("filling_provider").forGetter(config -> config.fillingProvider),
                        Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.byNameCodec()).fieldOf("inner_layer_provider").forGetter(config -> config.innerLayerProvider),
                        Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.byNameCodec()).fieldOf("alternate_inner_layer_provider").forGetter(config -> config.alternateInnerLayerProvider),
                        Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.byNameCodec()).fieldOf("middle_layer_provider").forGetter(config -> config.middleLayerProvider),
                        Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.byNameCodec()).fieldOf("outer_layer_provider").forGetter(config -> config.outerLayerProvider),
                        ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter(config -> config.innerPlacements),
                        TagKey.hashedCodec(Registries.BLOCK).fieldOf("cannot_replace").forGetter(config -> config.cannotReplace),
                        TagKey.hashedCodec(Registries.BLOCK).fieldOf("invalid_blocks").forGetter(config -> config.invalidBlocks),
                        TagPrefix.CODEC.optionalFieldOf("provider_material_prefix", TagPrefix.block).forGetter(config -> config.providerMaterialPrefix))
                .apply(instance, GeodeBlockSettings::new));
        // spotless:on
    }
}
