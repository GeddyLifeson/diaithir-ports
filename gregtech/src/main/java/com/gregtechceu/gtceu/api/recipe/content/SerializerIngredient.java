package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.serialization.Codec;

public class SerializerIngredient implements IContentSerializer<SizedIngredient> {

    public static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);
    public static SerializerIngredient INSTANCE = new SerializerIngredient();

    private SerializerIngredient() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, SizedIngredient content) {
        SizedIngredient.STREAM_CODEC.encode(buf, content);
    }

    @Override
    public SizedIngredient fromNetwork(RegistryFriendlyByteBuf buf) {
        return SizedIngredient.STREAM_CODEC.decode(buf);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SizedIngredient of(Object o) { return null; /* ported stub */ }

    @Override
    public SizedIngredient defaultValue() {
        return EMPTY;
    }

    @Override
    public Class<SizedIngredient> contentClass() {
        return SizedIngredient.class;
    }

    @Override
    public Codec<SizedIngredient> codec() {
        return SizedIngredient.NESTED_CODEC;
    }
}
