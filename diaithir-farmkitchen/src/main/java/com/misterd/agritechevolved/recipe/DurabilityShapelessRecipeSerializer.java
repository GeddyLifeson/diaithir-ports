package com.misterd.agritechevolved.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DurabilityShapelessRecipeSerializer {

    public static final MapCodec<DurabilityShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(DurabilityShapelessRecipe::category),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(DurabilityShapelessRecipe::getResultTemplate),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.getRecipeIngredients().stream().toList()),
                    Ingredient.CODEC.fieldOf("tool").forGetter(DurabilityShapelessRecipe::getToolIngredient),
                    Codec.INT.fieldOf("durability_per_item").forGetter(DurabilityShapelessRecipe::getDurabilityPerItem)
            ).apply(instance, (category, result, ingredients, tool, durability) -> {
                NonNullList<Ingredient> list = NonNullList.create();
                list.addAll(ingredients);
                return new DurabilityShapelessRecipe(category, result, list, tool, durability);
            })
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DurabilityShapelessRecipe> STREAM_CODEC =
            StreamCodec.of(DurabilityShapelessRecipeSerializer::toNetwork, DurabilityShapelessRecipeSerializer::fromNetwork);

    public static final RecipeSerializer<DurabilityShapelessRecipe> INSTANCE =
            new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private static void toNetwork(RegistryFriendlyByteBuf buf, DurabilityShapelessRecipe recipe) {
        CraftingBookCategory.STREAM_CODEC.encode(buf, recipe.category());
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getResultTemplate());
        buf.writeVarInt(recipe.getRecipeIngredients().size());
        for (Ingredient ing : recipe.getRecipeIngredients()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
        }
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getToolIngredient());
        buf.writeVarInt(recipe.getDurabilityPerItem());
    }

    private static DurabilityShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        CraftingBookCategory category = CraftingBookCategory.STREAM_CODEC.decode(buf);
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
        int count = buf.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(count);
        for (int i = 0; i < count; i++) {
            ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
        }
        Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        int durability = buf.readVarInt();
        return new DurabilityShapelessRecipe(category, result, ingredients, tool, durability);
    }
}