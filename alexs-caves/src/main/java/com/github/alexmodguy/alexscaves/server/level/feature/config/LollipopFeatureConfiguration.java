package com.github.alexmodguy.alexscaves.server.level.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public class LollipopFeatureConfiguration implements FeatureConfiguration {

    public static final Codec<LollipopFeatureConfiguration> CODEC = RecordCodecBuilder.create((configurationInstance) -> {
        return configurationInstance.group(Identifier.CODEC.listOf().fieldOf("big_lollipop_top_structures").forGetter((p_159830_) -> {
                    return p_159830_.bigLollipopTopStructures;
                }), Identifier.CODEC.listOf().fieldOf("small_lollipop_top_structures").forGetter((p_159830_) -> {
                    return p_159830_.smallLollipopTopStructures;
                }), Codec.FLOAT.fieldOf("big_chance").forGetter((otherConfig) -> {
            return otherConfig.bigChance;
        })
        ).apply(configurationInstance, LollipopFeatureConfiguration::new);
    });
    public final List<Identifier> bigLollipopTopStructures;
    public final List<Identifier> smallLollipopTopStructures;
    public final float bigChance;

    public LollipopFeatureConfiguration(List<Identifier> bigLollipopTopStructures, List<Identifier> smallLollipopTopStructures, float bigChance) {
        if (bigLollipopTopStructures.isEmpty() || smallLollipopTopStructures.isEmpty()) {
            throw new IllegalArgumentException("structure lists need at least one entry");
        } else {
            this.bigLollipopTopStructures = bigLollipopTopStructures;
            this.smallLollipopTopStructures = smallLollipopTopStructures;
            this.bigChance = bigChance;
        }
    }
}
