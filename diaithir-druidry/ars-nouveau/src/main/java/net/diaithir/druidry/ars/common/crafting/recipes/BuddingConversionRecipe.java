package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record BuddingConversionRecipe(Block input, Block result) implements SpecialSingleInputRecipe {


    public boolean matches(BlockState block) {
        return block.is(input);
    }

    @Override
    public boolean matches(SingleRecipeInput p_346065_, Level p_345375_) {
        return false;
    }

    @Override
    public RecipeSerializer<? extends BuddingConversionRecipe> getSerializer() {
        return Serializer.SERIALIZER;
    }

    @Override
    public RecipeType<? extends BuddingConversionRecipe> getType() {
        return RecipeRegistry.BUDDING_CONVERSION_TYPE.get();
    }


    public static class Serializer {
        public static final MapCodec<BuddingConversionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("input").forGetter(BuddingConversionRecipe::input),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("result").forGetter(BuddingConversionRecipe::result)
        ).apply(instance, BuddingConversionRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BuddingConversionRecipe> STREAM = CheatSerializer.create(CODEC);

        public static final RecipeSerializer<BuddingConversionRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM);



    }
}
