package sonar.fluxnetworks.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.crafting.FluxStorageRecipe;
import sonar.fluxnetworks.common.crafting.ConfigWipeRecipe;

public class RegistryRecipes {
    public static final Identifier FLUX_STORAGE_RECIPE_KEY = FluxNetworks.location("flux_storage_recipe");
    public static final Identifier CONFIG_WIPE_RECIPE_KEY = FluxNetworks.location("config_wipe_recipe");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluxStorageRecipe>> FLUX_STORAGE_RECIPE = DeferredHolder.create(BuiltInRegistries.RECIPE_SERIALIZER.key(), FLUX_STORAGE_RECIPE_KEY);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ConfigWipeRecipe>> CONFIG_WIPE_RECIPE = DeferredHolder.create(BuiltInRegistries.RECIPE_SERIALIZER.key(), CONFIG_WIPE_RECIPE_KEY);

    static void register(RegisterEvent.RegisterHelper<RecipeSerializer<?>> helper) {
        helper.register(FLUX_STORAGE_RECIPE_KEY, FluxStorageRecipe.SERIALIZER);
        helper.register(CONFIG_WIPE_RECIPE_KEY, ConfigWipeRecipe.SERIALIZER);
    }

    private RegistryRecipes() {}
}
