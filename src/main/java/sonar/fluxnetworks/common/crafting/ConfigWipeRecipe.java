package sonar.fluxnetworks.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.common.block.FluxStorageBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

/**
 * save Flux Storage energy when wiping NBT
 */
@ParametersAreNonnullByDefault
public class ConfigWipeRecipe extends ShapelessRecipe {
    public static final RecipeSerializer<ConfigWipeRecipe> SERIALIZER = new RecipeSerializer<>(
            ShapelessRecipe.MAP_CODEC.xmap(ConfigWipeRecipe::new, Function.identity()),
            ShapelessRecipe.STREAM_CODEC.map(ConfigWipeRecipe::new, Function.identity()));

    public ConfigWipeRecipe(ShapelessRecipe recipe) {
        super(recipe.commonInfo, recipe.bookInfo, recipe.result(), recipe.ingredients);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack originalStack = input.items().stream().filter(stack -> !stack.isEmpty() && stack.get(FluxDataComponents.FLUX_CONFIG) != null).findFirst().orElse(null);
        ItemStack output = super.assemble(input);

        if (originalStack != null) {
            if (Block.byItem(output.getItem()) instanceof FluxStorageBlock) {
                output.copyFrom(originalStack, FluxDataComponents.STORED_ENERGY);
            }
            return output;
        }
        return output;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }
}