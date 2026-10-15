package sonar.fluxnetworks.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import sonar.fluxnetworks.api.FluxDataComponents;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class FluxStorageRecipe extends ShapedRecipe {
    public static final RecipeSerializer<FluxStorageRecipe> SERIALIZER = new RecipeSerializer<>(
            ShapedRecipe.MAP_CODEC.xmap(FluxStorageRecipe::new, Function.identity()),
            ShapedRecipe.STREAM_CODEC.map(FluxStorageRecipe::new, Function.identity()));

    public FluxStorageRecipe(ShapedRecipe recipe) {
        super(recipe.commonInfo, recipe.bookInfo, recipe.pattern, recipe.result);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = super.assemble(input);
        // Find all flux storage items in the input
        List<ItemStack> storageItems = input.items().stream().filter(stack -> !stack.isEmpty() && stack.get(FluxDataComponents.STORED_ENERGY) != null).toList();
        // If none found, bail
        if (storageItems.isEmpty()) {
            return result;
        }
        // Sum the energy of all storage items
        long totalEnergy = storageItems.stream().map(stack -> stack.get(FluxDataComponents.STORED_ENERGY)).reduce(0L, Long::sum);
        // Put the total energy value into the resulting item
        result.set(FluxDataComponents.STORED_ENERGY, totalEnergy);
        // Copy device configuration from the first storage item (if present)
        result.copyFrom(storageItems.getFirst(), FluxDataComponents.FLUX_CONFIG);

        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }
}