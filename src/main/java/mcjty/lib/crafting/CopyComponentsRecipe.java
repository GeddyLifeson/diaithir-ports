package mcjty.lib.crafting;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nonnull;

public class CopyComponentsRecipe extends ShapedRecipe {

    // JSON: {"type": "mcjtylib:copy_components", "recipe": <a shaped recipe>}
    public static final RecipeSerializer<CopyComponentsRecipe> SERIALIZER = new RecipeSerializer<>(
            RecordCodecBuilder.mapCodec(i -> i.group(
                    ShapedRecipe.MAP_CODEC.codec().fieldOf("recipe").forGetter(r -> r)
            ).apply(i, CopyComponentsRecipe::new)),
            ShapedRecipe.STREAM_CODEC.map(CopyComponentsRecipe::new, r -> r));

    public CopyComponentsRecipe(ShapedRecipe recipe) {
        super(recipe.commonInfo, recipe.bookInfo, recipe.pattern, recipe.result);
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack result = super.assemble(inv);
        CompoundTag nbt = null;
        for (int i = 0 ; i < inv.size() ; i++) {
            ItemStack stack = inv.getItem(i);

            IComponentsToPreserve inbt = null;
            if (stack.getItem() instanceof IComponentsToPreserve ingredient) {
                inbt = ingredient;
            } else if (stack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof IComponentsToPreserve ingredient) {
                    inbt = ingredient;
                }
            }

            if (inbt != null) {
                for (DataComponentType type : inbt.getComponentsToPreserve()) {
                    Object o = stack.get(type);
                    if (o != null) {
                        result.set(type, o);
                    }
                }
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }
}
