package com.github.alexmodguy.alexscaves.server.level.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** Diaithir: replaces upstream's 3D cave biome source. One underground chamber of a cave biome, dropped into any host biome. */
public record CaveChamberConfiguration(Holder<Biome> biome, BlockState floor, BlockState wall, BlockState ceiling,
                                       Optional<BlockState> fluid, int fluidDepth, int radius, int height, int minY, int maxY,
                                       List<Holder<ConfiguredFeature<?, ?>>> decor, int decorAttempts) implements FeatureConfiguration {
    public static final Codec<CaveChamberConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            Biome.CODEC.fieldOf("biome").forGetter(CaveChamberConfiguration::biome),
            BlockState.CODEC.fieldOf("floor").forGetter(CaveChamberConfiguration::floor),
            BlockState.CODEC.fieldOf("wall").forGetter(CaveChamberConfiguration::wall),
            BlockState.CODEC.fieldOf("ceiling").forGetter(CaveChamberConfiguration::ceiling),
            BlockState.CODEC.optionalFieldOf("fluid").forGetter(CaveChamberConfiguration::fluid),
            Codec.INT.optionalFieldOf("fluid_depth", 3).forGetter(CaveChamberConfiguration::fluidDepth),
            Codec.INT.optionalFieldOf("radius", 13).forGetter(CaveChamberConfiguration::radius),
            Codec.INT.optionalFieldOf("height", 9).forGetter(CaveChamberConfiguration::height),
            Codec.INT.fieldOf("min_y").forGetter(CaveChamberConfiguration::minY),
            Codec.INT.fieldOf("max_y").forGetter(CaveChamberConfiguration::maxY),
            ConfiguredFeature.CODEC.listOf().optionalFieldOf("decor", List.of()).forGetter(CaveChamberConfiguration::decor),
            Codec.INT.optionalFieldOf("decor_attempts", 40).forGetter(CaveChamberConfiguration::decorAttempts)
    ).apply(i, CaveChamberConfiguration::new));
}
