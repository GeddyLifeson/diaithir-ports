package com.misterd.agritechevolved.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record DropEntry(Item item, int min, int max, float chance) {

    public static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(DropEntry::item),
            Codec.INT.fieldOf("min").forGetter(DropEntry::min),
            Codec.INT.fieldOf("max").forGetter(DropEntry::max),
            Codec.FLOAT.fieldOf("chance").forGetter(DropEntry::chance)
    ).apply(instance, DropEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DropEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), DropEntry::item,
            ByteBufCodecs.INT, DropEntry::min,
            ByteBufCodecs.INT, DropEntry::max,
            ByteBufCodecs.FLOAT, DropEntry::chance,
            DropEntry::new
    );
}