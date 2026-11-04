package dev.shadowsoffire.apothic_enchanting.table.infusion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class KeepNBTInfusionRecipe extends InfusionRecipe {

    public static final MapCodec<KeepNBTInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(InfusionRecipe::getOutput),
        Ingredient.CODEC.fieldOf("input").forGetter(InfusionRecipe::getInput),
        Stats.CODEC.fieldOf("requirements").forGetter(InfusionRecipe::getRequirements),
        Stats.CODEC.optionalFieldOf("max_requirements", NO_MAX).forGetter(InfusionRecipe::getMaxRequirements))
        .apply(inst, KeepNBTInfusionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, KeepNBTInfusionRecipe> STREAM_CODEC = StreamCodec.composite(
        ItemStackTemplate.STREAM_CODEC, InfusionRecipe::getOutput,
        Ingredient.CONTENTS_STREAM_CODEC, InfusionRecipe::getInput,
        Stats.STREAM_CODEC, InfusionRecipe::getRequirements,
        Stats.STREAM_CODEC, InfusionRecipe::getMaxRequirements,
        KeepNBTInfusionRecipe::new);

    public static final RecipeSerializer<KeepNBTInfusionRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public KeepNBTInfusionRecipe(ItemStackTemplate output, Ingredient input, Stats requirements, Stats maxRequirements) {
        super(output, input, requirements, maxRequirements);
    }

    @Override
    public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
        ItemStack out = this.getOutput().create();
        if (!input.isComponentsPatchEmpty()) {
            out.applyComponentsAndValidate(input.getComponentsPatch());
        }
        return out;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return KeepNBTInfusionRecipe.SERIALIZER;
    }

}
