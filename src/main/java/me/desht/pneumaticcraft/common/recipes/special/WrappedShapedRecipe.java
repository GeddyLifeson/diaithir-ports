package me.desht.pneumaticcraft.common.recipes.special;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A crafting recipe which behaves as the wrapped shaped recipe, but has its own serializer.
 */
public abstract class WrappedShapedRecipe extends NormalCraftingRecipe {
    protected final ShapedRecipe wrapped;

    public WrappedShapedRecipe(ShapedRecipe wrapped) {
        super(new Recipe.CommonInfo(wrapped.showNotification()),
                new CraftingRecipe.CraftingBookInfo(wrapped.category(), wrapped.group()));
        this.wrapped = wrapped;
    }

    public ShapedRecipe wrapped() {
        return wrapped;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return wrapped.placementInfo();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return wrapped.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return wrapped.assemble(input);
    }

    @Override
    public List<RecipeDisplay> display() {
        return wrapped.display();
    }
}
