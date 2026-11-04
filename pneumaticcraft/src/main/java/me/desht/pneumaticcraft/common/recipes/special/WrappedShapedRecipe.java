package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.mixin.accessors.ShapedRecipeAccess;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public abstract class WrappedShapedRecipe extends ShapedRecipe {
    protected final ShapedRecipe wrapped;

    public WrappedShapedRecipe(ShapedRecipe wrapped) {
        super(new Recipe.CommonInfo(wrapped.showNotification()),
                new CraftingRecipe.CraftingBookInfo(wrapped.category(), wrapped.group()),
                wrapped.pattern,
                ((ShapedRecipeAccess) wrapped).getResult());

        this.wrapped = wrapped;
    }

    public ShapedRecipe wrapped() {
        return wrapped;
    }
}
