package com.github.alexmodguy.alexscaves.server.level.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public class WhalefallFeatureConfiguration implements FeatureConfiguration {

    public static final Codec<WhalefallFeatureConfiguration> CODEC = RecordCodecBuilder.create((configurationInstance) -> {
        return configurationInstance.group(Identifier.CODEC.listOf().fieldOf("head_structures").forGetter((p_159830_) -> {
                    return p_159830_.headStructures;
                }), Identifier.CODEC.listOf().fieldOf("body_structures").forGetter((p_159830_) -> {
                    return p_159830_.bodyStructures;
                }), Identifier.CODEC.listOf().fieldOf("tail_structures").forGetter((p_159830_) -> {
                    return p_159830_.tailStructures;
                })
        ).apply(configurationInstance, WhalefallFeatureConfiguration::new);
    });
    public final List<Identifier> headStructures;
    public final List<Identifier> bodyStructures;
    public final List<Identifier> tailStructures;

    public WhalefallFeatureConfiguration(List<Identifier> headStructures, List<Identifier> bodyStructures, List<Identifier> tailStructures) {
        if (headStructures.isEmpty() || bodyStructures.isEmpty() || tailStructures.isEmpty()) {
            throw new IllegalArgumentException("structure lists need at least one entry");
        } else {
            this.headStructures = headStructures;
            this.bodyStructures = bodyStructures;
            this.tailStructures = tailStructures;
        }
    }
}
