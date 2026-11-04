package com.misterd.agritechevolved.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SoilModifierData(float growthModifier) {
    public static final Codec<SoilModifierData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("growth_modifier").forGetter(SoilModifierData::growthModifier)
    ).apply(instance, SoilModifierData::new));
}