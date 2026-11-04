package com.diaithir.overworldbiomes.naturesspirit.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Replacement for the removed vanilla RandomPatchConfiguration (tries, spreads, placed feature). */
public record LeveledRandomPatchConfig(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements FeatureConfiguration {
  public static final Codec<LeveledRandomPatchConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
      Codec.intRange(1, 128).fieldOf("tries").orElse(128).forGetter(LeveledRandomPatchConfig::tries),
      Codec.intRange(0, 128).fieldOf("xz_spread").orElse(7).forGetter(LeveledRandomPatchConfig::xzSpread),
      Codec.intRange(0, 128).fieldOf("y_spread").orElse(3).forGetter(LeveledRandomPatchConfig::ySpread),
      PlacedFeature.CODEC.fieldOf("feature").forGetter(LeveledRandomPatchConfig::feature)
  ).apply(i, LeveledRandomPatchConfig::new));
}
