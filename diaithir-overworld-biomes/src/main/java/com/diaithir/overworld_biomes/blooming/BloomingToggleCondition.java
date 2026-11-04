package com.diaithir.overworld_biomes.blooming;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

public record BloomingToggleCondition(String key) implements ICondition {
    public static final MapCodec<BloomingToggleCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            com.mojang.serialization.Codec.STRING.fieldOf("key").forGetter(BloomingToggleCondition::key)
    ).apply(i, BloomingToggleCondition::new));

    @Override
    public boolean test(ICondition.IContext context) {
        return BloomingConfig.test(key);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
