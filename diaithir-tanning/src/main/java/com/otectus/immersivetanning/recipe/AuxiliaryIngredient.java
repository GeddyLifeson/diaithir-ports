package com.otectus.immersivetanning.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** A non-leather ingredient (string, iron, a chest...) consumed from the leatherworker's inventory when stitching starts. */
public record AuxiliaryIngredient(Ingredient ingredient, int count) {
    public static final Codec<AuxiliaryIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(AuxiliaryIngredient::ingredient),
            ExtraCodecs.intRange(1, 64).optionalFieldOf("count", 1).forGetter(AuxiliaryIngredient::count)
    ).apply(i, AuxiliaryIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AuxiliaryIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, AuxiliaryIngredient::ingredient,
            ByteBufCodecs.VAR_INT, AuxiliaryIngredient::count,
            AuxiliaryIngredient::new);

    /** A representative stack for display, with this ingredient's count. */
    public ItemStack displayStack() {
        return ingredient.items().findFirst().map(Holder::value).map(Item::getDefaultInstance)
                .map(s -> s.copyWithCount(count)).orElse(ItemStack.EMPTY);
    }
}
