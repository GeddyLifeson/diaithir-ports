package net.diaithir.forgeworks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Optional;

/**
 * One data-driven recipe for any of the machines (the recipe type decides which machine runs it). Fields:
 * inputs (ingredient plus count each), result (item, optional), bonus plus bonus_chance (optional extra item), fluid
 * (id plus amount in mB, optional), energy (FE for the whole recipe) and time (ticks at speed 1).
 */
public final class MachineRecipe implements Recipe<MachineRecipe.Inputs> {
    public record Input(Ingredient ingredient, int count) {
        static final Codec<Input> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(Input::ingredient),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(Input::count)).apply(i, Input::new));
    }

    public record FluidOut(Fluid fluid, int amount) {
        static final Codec<FluidOut> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidOut::fluid),
                ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidOut::amount)).apply(i, FluidOut::new));
    }

    /** What the machine hands to the recipe: its input slots. */
    public record Inputs(List<ItemStack> stacks) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return stacks.get(index);
        }

        @Override
        public int size() {
            return stacks.size();
        }
    }

    public final Kind kind;
    public final List<Input> inputs;
    public final Optional<ItemStackTemplate> result;
    public final Optional<ItemStackTemplate> bonus;
    public final float bonusChance;
    public final Optional<FluidOut> fluid;
    public final int energy;
    public final int time;

    public MachineRecipe(Kind kind, List<Input> inputs, Optional<ItemStackTemplate> result, Optional<ItemStackTemplate> bonus, float bonusChance,
            Optional<FluidOut> fluid, int energy, int time) {
        this.kind = kind;
        this.inputs = inputs;
        this.result = result;
        this.bonus = bonus;
        this.bonusChance = bonusChance;
        this.fluid = fluid;
        this.energy = energy;
        this.time = time;
    }

    public static MapCodec<MachineRecipe> codec(Kind kind) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Input.CODEC.listOf(1, 2).fieldOf("inputs").forGetter(r -> r.inputs),
                ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(r -> r.result),
                ItemStackTemplate.CODEC.optionalFieldOf("bonus").forGetter(r -> r.bonus),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("bonus_chance", 0.0F).forGetter(r -> r.bonusChance),
                FluidOut.CODEC.optionalFieldOf("fluid").forGetter(r -> r.fluid),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("energy", 2000).forGetter(r -> r.energy),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("time", 200).forGetter(r -> r.time))
                .apply(i, (in, res, bon, ch, fl, en, ti) -> new MachineRecipe(kind, in, res, bon, ch, fl, en, ti)));
    }

    public static RecipeSerializer<MachineRecipe> serializer(Kind kind) {
        MapCodec<MachineRecipe> codec = codec(kind);
        StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> stream = ByteBufCodecs.fromCodecWithRegistries(codec.codec());
        return new RecipeSerializer<>(codec, stream);
    }

    /** Which recipe input goes with which machine slot (0 or 1), or null if the stacks do not satisfy the recipe. */
    public int[] assign(List<ItemStack> stacks) {
        int n = inputs.size();
        if (n == 1) return fits(inputs.get(0), stacks.get(0)) ? new int[] {0} : null;
        if (fits(inputs.get(0), stacks.get(0)) && fits(inputs.get(1), stacks.get(1))) return new int[] {0, 1};
        if (fits(inputs.get(0), stacks.get(1)) && fits(inputs.get(1), stacks.get(0))) return new int[] {1, 0};
        return null;
    }

    private static boolean fits(Input in, ItemStack s) {
        return !s.isEmpty() && s.getCount() >= in.count() && in.ingredient().test(s);
    }

    @Override
    public boolean matches(Inputs input, Level level) {
        return input.size() >= kind.inputs && assign(input.stacks()) != null;
    }

    @Override
    public ItemStack assemble(Inputs input) {
        return result.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<Inputs>> getSerializer() {
        return Machines.RECIPE_SERIALIZERS.get(kind).get();
    }

    @Override
    public RecipeType<? extends Recipe<Inputs>> getType() {
        return Machines.RECIPE_TYPES.get(kind).get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return Machines.BOOK_CATEGORY.get();
    }
}
