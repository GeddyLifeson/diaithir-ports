package net.diaithir.crafting.foundry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Data component on magazine, drum, clip, belt, box and cell stacks (plans/MUNITIONS_CONTAINERS.md). {@code round} is an item id, empty when count is 0. */
public record AmmoContainer(String weaponClass, int capacity, int count, String round) {
    public static final Codec<AmmoContainer> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("class").forGetter(AmmoContainer::weaponClass),
            Codec.intRange(1, 100000).fieldOf("capacity").forGetter(AmmoContainer::capacity),
            Codec.intRange(0, 100000).optionalFieldOf("count", 0).forGetter(AmmoContainer::count),
            Codec.STRING.optionalFieldOf("round", "").forGetter(AmmoContainer::round)).apply(i, AmmoContainer::new));
}
