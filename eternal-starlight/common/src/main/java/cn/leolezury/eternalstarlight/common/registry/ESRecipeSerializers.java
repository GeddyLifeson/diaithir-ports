package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.item.recipe.*;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

public class ESRecipeSerializers {
	public static final RegistrationProvider<RecipeSerializer<?>> RECIPE_SERIALIZERS = RegistrationProvider.get(Registries.RECIPE_SERIALIZER, EternalStarlight.ID);

	public static final RegistryObject<RecipeSerializer<?>, RecipeSerializer<GeyserSmokingRecipe>> GEYSER_SMOKING = RECIPE_SERIALIZERS.register("geyser_smoking", () -> new GeyserSmokingRecipe.Serializer().build());
	public static final RegistryObject<RecipeSerializer<?>, RecipeSerializer<DryingRecipe>> DRYING = RECIPE_SERIALIZERS.register("drying", () -> new DryingRecipe.Serializer().build());
	public static final RegistryObject<RecipeSerializer<?>, RecipeSerializer<ToolModificationRecipe>> TOOL_MODIFICATION = RECIPE_SERIALIZERS.register("tool_modification", () -> new ToolModificationRecipe.Serializer().build());
	public static final RegistryObject<RecipeSerializer<?>, RecipeSerializer<AccessoryCombinationRecipe>> ACCESSORY_COMBINATION = RECIPE_SERIALIZERS.register("accessory_combination", () -> new RecipeSerializer<>(MapCodec.unit(() -> new AccessoryCombinationRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)), StreamCodec.of((buf, recipe) -> { }, buf -> new AccessoryCombinationRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC))));
	public static final RegistryObject<RecipeSerializer<?>, RecipeSerializer<AlloyRecipe>> ALLOY = RECIPE_SERIALIZERS.register("alloy", () -> new AlloyRecipe.Serializer().build());
	public static final RegistryObject<RecipeSerializer<?>, RecipeSerializer<EtherConversionRecipe>> ETHER_CONVERSION = RECIPE_SERIALIZERS.register("ether_conversion", () -> new EtherConversionRecipe.Serializer().build());

	public static void loadClass() {
	}
}
