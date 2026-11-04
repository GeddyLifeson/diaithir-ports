package com.misterd.agritechevolved.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FertilizerData(float speedMultiplier, float yieldMultiplier) {
    public static final Codec<FertilizerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed_multiplier").forGetter(FertilizerData::speedMultiplier),
            Codec.FLOAT.fieldOf("yield_multiplier").forGetter(FertilizerData::yieldMultiplier)
    ).apply(instance, FertilizerData::new));
}