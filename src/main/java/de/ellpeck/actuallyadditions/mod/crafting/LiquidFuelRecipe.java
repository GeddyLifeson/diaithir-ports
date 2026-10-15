package de.ellpeck.actuallyadditions.mod.crafting;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.PlacementInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class LiquidFuelRecipe implements Recipe<RecipeInput> {
    public static String NAME = "liquid_fuel";
    //TODO 1.22 change from FluidStack to Neo:FluidIngredient
    private FluidStack fuel;
    private int burnTime;
    private int totalEnergy;

    /**
     * Oil generator recipe
     * @param fuel The fluid
     * @param totalEnergy The total power generated.
     * @param burnTime The length the fluid burns for, in ticks.
     */
    public LiquidFuelRecipe( FluidStack fuel, int totalEnergy, int burnTime) {
        this.fuel = fuel;
        this.burnTime = burnTime;
        this.totalEnergy = totalEnergy;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getTotalEnergy() {
        return totalEnergy;
    }

    @Override
    public boolean matches(@Nonnull RecipeInput pInv,@Nonnull Level pLevel) {
        return false;
    }

    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(this.fuel, stack);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public int getFuelAmount() {
        return this.fuel.getAmount();
    }

    public FluidStack getFuel() {
        return fuel;
    }

    @Nonnull
    @Override
    public ItemStack assemble(RecipeInput pInv) {
        return ItemStack.EMPTY;
    }


    @Nonnull
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ActuallyRecipes.LIQUID_FUEL_RECIPE.get();
    }

    @Nonnull
    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ActuallyRecipes.Types.LIQUID_FUEL.get();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    // machine recipes: never placed by the recipe book
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer {
        private static final MapCodec<LiquidFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                FluidStack.CODEC.fieldOf("fuel").forGetter(recipe -> recipe.fuel),
                                Codec.INT.fieldOf("total_energy").forGetter(recipe -> recipe.totalEnergy),
                                Codec.INT.fieldOf("burn_time").forGetter(recipe -> recipe.burnTime)
                        )
                        .apply(instance, LiquidFuelRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, LiquidFuelRecipe> STREAM_CODEC = StreamCodec.of(
                LiquidFuelRecipe.Serializer::toNetwork, LiquidFuelRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<LiquidFuelRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public MapCodec<LiquidFuelRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, LiquidFuelRecipe> streamCodec() {
            return STREAM_CODEC;
        }

//        @Nonnull
//        @Override
//        public LiquidFuelRecipe fromJson(@Nonnull Identifier pId, JsonObject pJson) {
//            JsonObject ingredient = pJson.getAsJsonObject("ingredient");
//
//            Identifier fluidRes = Identifier.tryParse(GsonHelper.getAsString(ingredient, "fluid"));
//            Fluid fluid = BuiltInRegistries.FLUIDS.getValue(fluidRes);
//            if (fluid == null)
//                throw new JsonParseException("Unknown fluid '" + fluidRes + "'");
//            int inputAmount = GsonHelper.getAsInt(ingredient, "amount", 50);
//            FluidStack input = new FluidStack(fluid, inputAmount);
//
//            JsonObject result = pJson.getAsJsonObject("result");
//            int totalEnergy = result.get("total_energy").getAsInt();
//            int burnTime = result.get("burn_time").getAsInt();
//            return new LiquidFuelRecipe(pId, input, totalEnergy, burnTime);
//        }

        public static LiquidFuelRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf pBuffer) {
            FluidStack input = FluidStack.STREAM_CODEC.decode(pBuffer);
            int totalEnergy = pBuffer.readInt();
            int burnTime = pBuffer.readInt();
            return new LiquidFuelRecipe(input, totalEnergy, burnTime);
        }

        public static void toNetwork(@Nonnull RegistryFriendlyByteBuf pBuffer, LiquidFuelRecipe pRecipe) {
            FluidStack.STREAM_CODEC.encode(pBuffer, pRecipe.fuel);
            pBuffer.writeInt(pRecipe.totalEnergy);
            pBuffer.writeInt(pRecipe.burnTime);
        }
    }
}
