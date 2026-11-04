package net.diaithir.vehicles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Contents of a jerry can: a fuel id from the carrier table (diaithir-units fuels.csv) and litres. */
public record CanContent(String fuel, double litres) {
    public static final CanContent EMPTY = new CanContent("", 0);
    public static final Codec<CanContent> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("fuel").forGetter(CanContent::fuel),
            Codec.DOUBLE.fieldOf("litres").forGetter(CanContent::litres)).apply(i, CanContent::new));
}
