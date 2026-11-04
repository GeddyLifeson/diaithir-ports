package com.misterd.agritechevolved.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FarmlandRecipeCategory implements IRecipeCategory<FarmlandRecipe> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath("agritechevolved", "farmland_tilling");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("agritechevolved", "textures/gui/jei/jei_hoe_farmland_gui.png");
    public static final IRecipeType<FarmlandRecipe> FARMLAND_RECIPE_TYPE = IRecipeType.create(UID, FarmlandRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FarmlandRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 108, 36);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.FARMLAND));
    }

    @Override
    public IRecipeType<FarmlandRecipe> getRecipeType() {
        return FARMLAND_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.agritechevolved.farmland_tilling.title");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() { return 108; }

    @Override
    public int getHeight() { return 36; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FarmlandRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .addIngredients(recipe.getSoilInput());

        builder.addSlot(RecipeIngredientRole.INPUT, 46, 10)
                .addIngredients(recipe.getHoeInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 10)
                .add(recipe.getOutput());
    }

    @Override
    public void draw(FarmlandRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}