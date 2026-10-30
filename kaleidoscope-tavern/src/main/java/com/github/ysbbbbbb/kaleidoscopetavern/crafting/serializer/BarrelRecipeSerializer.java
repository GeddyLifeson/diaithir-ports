package com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Objects;

public class BarrelRecipeSerializer {
    public static final ResourceKey<Recipe<?>> EMPTY_RECIPE_ID = ResourceKey.create(Registries.RECIPE, KaleidoscopeTavern.modLoc("empty"));
    public static final int DEFAULT_UNIT_TIME = 2400;
    public static final int MAX_INGREDIENTS = 4;

    public static final MapCodec<BarrelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.sizeLimitedListOf(MAX_INGREDIENTS).optionalFieldOf("ingredients", Lists.newArrayList()).forGetter(BarrelRecipe::ingredients),
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(BarrelRecipe::fluid),
            Ingredient.CODEC.fieldOf("carrier").forGetter(BarrelRecipe::carrier),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(BarrelRecipe::result),
            Codec.INT.optionalFieldOf("unit_time", DEFAULT_UNIT_TIME).forGetter(BarrelRecipe::unitTime)
    ).apply(instance, BarrelRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BarrelRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BarrelRecipe decode(RegistryFriendlyByteBuf buf) {
            List<Ingredient> ingredients = Lists.newArrayList();
            int size = Math.min(MAX_INGREDIENTS, buf.readVarInt());
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            }
            Identifier fluidId = buf.readIdentifier();
            Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId);
            Ingredient carrier = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
            int unitTime = buf.readVarInt();
            return new BarrelRecipe(ingredients, fluid, carrier, result, unitTime);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BarrelRecipe recipe) {
            var nonEmpty = recipe.ingredients().stream().filter(i -> !i.isEmpty()).toList();
            buf.writeVarInt(nonEmpty.size());
            for (Ingredient ingredient : nonEmpty) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }
            Identifier fluidId = BuiltInRegistries.FLUID.getKey(recipe.fluid());
            buf.writeIdentifier(Objects.requireNonNull(fluidId));
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.carrier());
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result());
            buf.writeVarInt(recipe.unitTime());
        }
    };
}
