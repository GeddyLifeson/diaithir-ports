package de.ellpeck.actuallyadditions.mod.crafting;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ActuallyRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ActuallyAdditions.MODID);

    public static void init(IEventBus bus) {
        SERIALIZERS.register(bus);
        Ingredients.INGREDIENTS.register(bus);
        Types.RECIPE_TYPES.register(bus);
    }

    public static final Supplier<RecipeSerializer<RecipeKeepDataShaped>> KEEP_DATA_SHAPED_RECIPE = SERIALIZERS.register(RecipeKeepDataShaped.NAME, () -> RecipeKeepDataShaped.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<RecipeKeepDataShapeless>> KEEP_DATA_SHAPELESS_RECIPE = SERIALIZERS.register(RecipeKeepDataShapeless.NAME, () -> RecipeKeepDataShapeless.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<LaserRecipe>> LASER_RECIPE = SERIALIZERS.register(LaserRecipe.NAME, () -> LaserRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<EmpowererRecipe>> EMPOWERING_RECIPE = SERIALIZERS.register(EmpowererRecipe.NAME, () -> EmpowererRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<CrushingRecipe>> CRUSHING_RECIPE = SERIALIZERS.register(CrushingRecipe.NAME, () -> CrushingRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<LiquidFuelRecipe>> LIQUID_FUEL_RECIPE = SERIALIZERS.register(LiquidFuelRecipe.NAME, () -> LiquidFuelRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<PressingRecipe>> PRESSING_RECIPE = SERIALIZERS.register(PressingRecipe.NAME, () -> PressingRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<FermentingRecipe>> FERMENTING_RECIPE = SERIALIZERS.register(FermentingRecipe.NAME, () -> FermentingRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<ColorChangeRecipe>> COLOR_CHANGE_RECIPE = SERIALIZERS.register(ColorChangeRecipe.NAME, () -> ColorChangeRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<MiningLensRecipe>> MINING_LENS_RECIPE = SERIALIZERS.register(MiningLensRecipe.NAME, () -> MiningLensRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<CoffeeIngredientRecipe>> COFFEE_INGREDIENT_RECIPE = SERIALIZERS.register(CoffeeIngredientRecipe.NAME, () -> CoffeeIngredientRecipe.Serializer.INSTANCE);



    public static class Types {
        public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, ActuallyAdditions.MODID);
        
        public static final Supplier<RecipeType<LaserRecipe>> LASER = RECIPE_TYPES.register("laser", RecipeType::simple);
        public static final Supplier<RecipeType<EmpowererRecipe>> EMPOWERING = RECIPE_TYPES.register("empower", RecipeType::simple);
        public static final Supplier<RecipeType<CrushingRecipe>> CRUSHING = RECIPE_TYPES.register("crushing", RecipeType::simple);
        public static final Supplier<RecipeType<LiquidFuelRecipe>> LIQUID_FUEL = RECIPE_TYPES.register("liquid_fuel", RecipeType::simple);
        public static final Supplier<RecipeType<PressingRecipe>> PRESSING = RECIPE_TYPES.register("pressing", RecipeType::simple);
        public static final Supplier<RecipeType<FermentingRecipe>> FERMENTING = RECIPE_TYPES.register("fermenting", RecipeType::simple);
        public static final Supplier<RecipeType<ColorChangeRecipe>> COLOR_CHANGE = RECIPE_TYPES.register("color_change", RecipeType::simple);
        public static final Supplier<RecipeType<MiningLensRecipe>> MINING_LENS = RECIPE_TYPES.register("mining_lens", RecipeType::simple);
        public static final Supplier<RecipeType<CoffeeIngredientRecipe>> COFFEE_INGREDIENT = RECIPE_TYPES.register("coffee_ingredient", RecipeType::simple);
    }
    public static class Ingredients {
        public static final DeferredRegister<IngredientType<?>> INGREDIENTS = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, ActuallyAdditions.MODID);

        //TODO change name from nbt in 1.22
        public static final Supplier<IngredientType<TargetComponentIngredient>> TARGET_NBT = INGREDIENTS.register("target_nbt", () -> new IngredientType<>(TargetComponentIngredient.CODEC));
    }
}
