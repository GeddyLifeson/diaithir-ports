package net.id.paradise_lost.world.feature.configs;

import net.minecraft.util.valueproviders.IntProviders;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record JaggedOreConfig(BlockStateProvider block, IntProvider height, IntProvider width, IntProvider length, IntProvider lengthOffset) implements FeatureConfiguration {
    public static final Codec<JaggedOreConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("block").forGetter(JaggedOreConfig::block),
            IntProviders.CODEC.fieldOf("height").forGetter(JaggedOreConfig::height),
            IntProviders.CODEC.fieldOf("width").forGetter(JaggedOreConfig::width),
            IntProviders.CODEC.fieldOf("length").forGetter(JaggedOreConfig::length),
            IntProviders.CODEC.fieldOf("length_offset").forGetter(JaggedOreConfig::lengthOffset)
    ).apply(instance, JaggedOreConfig::new));
}
