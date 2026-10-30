package com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category;


import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IPressingTub;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PressingTubCategory implements IRecipeCategory<RecipeHolder<PressingTubRecipe>> {
    public static final IRecipeHolderType<PressingTubRecipe> TYPE = IRecipeType.create(ModRecipes.PRESSING_TUB_RECIPE.get());

    private static final Identifier BG = KaleidoscopeTavern.modLoc("textures/gui/jei/pressing_tub.png");
    private static final MutableComponent TITLE = Component.translatable("item.kaleidoscope_tavern.pressing_tub");

    public static final int WIDTH = 155;
    public static final int HEIGHT = 50;

    private final IDrawable bgDraw;
    private final IDrawable iconDraw;

    public PressingTubCategory(IGuiHelper guiHelper) {
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.iconDraw = guiHelper.createDrawableItemLike(ModItems.PRESSING_TUB.get());
    }

    @Override
    public void draw(RecipeHolder<PressingTubRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.bgDraw.draw(guiGraphics);

        PressingTubRecipe recipe = holder.value();
        int needPressCount = IPressingTub.MAX_FLUID_AMOUNT / recipe.getFluidAmount();
        if (needPressCount * recipe.getFluidAmount() < IPressingTub.MAX_FLUID_AMOUNT) {
            needPressCount++;
        }
        Component tip = Component.translatable("jei.kaleidoscope_tavern.pressing_tub.need_press_count", needPressCount);
        drawCenteredString(guiGraphics, tip);
    }

    private void drawCenteredString(GuiGraphicsExtractor guiGraphics, Component text) {
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence sequence = text.getVisualOrderText();
        guiGraphics.text(font, sequence, (WIDTH - font.width(sequence) - 5), 42, 0x555555, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<PressingTubRecipe> holder, IFocusGroup focuses) {
        PressingTubRecipe recipe = holder.value();
        int needPressCount = IPressingTub.MAX_FLUID_AMOUNT / recipe.getFluidAmount();
        if (needPressCount * recipe.getFluidAmount() < IPressingTub.MAX_FLUID_AMOUNT) {
            needPressCount++;
        }

        Ingredient input = recipe.input();
        int finalNeedPressCount = needPressCount;
        List<ItemStack> list = input.items()
                .map(holderItem -> new ItemStack(holderItem, finalNeedPressCount))
                .toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 13)
                .addIngredients(VanillaTypes.ITEM_STACK, list);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 18).add(recipe.getFluid().getBucket());
    }

    @Override
    public IRecipeType<RecipeHolder<PressingTubRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    @Nullable
    public IDrawable getIcon() {
        return iconDraw;
    }
}
