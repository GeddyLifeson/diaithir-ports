package net.diaithir.vehicles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Contents of a jerry can, stored as a data component. */
public record CanContent(String fuel, long amount) {
    public static final CanContent EMPTY = new CanContent("", 0);
    public static final Codec<CanContent> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("fuel").forGetter(CanContent::fuel),
            Codec.LONG.fieldOf("amount").forGetter(CanContent::amount)).apply(i, CanContent::new));
}
