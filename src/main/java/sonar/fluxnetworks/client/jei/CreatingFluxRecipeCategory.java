package sonar.fluxnetworks.client.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.register.RegistryBlocks;
import sonar.fluxnetworks.register.RegistryItems;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class CreatingFluxRecipeCategory implements IRecipeCategory<CreatingFluxRecipe> {

    public static final Identifier TEXTURES = FluxNetworks.location(
            "textures/gui/gui_creating_flux_recipe.png");

    public static final RecipeType<CreatingFluxRecipe> RECIPE_TYPE =
            RecipeType.create(FluxNetworks.MODID, "creating_flux", CreatingFluxRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer timer;

    public CreatingFluxRecipeCategory(@Nonnull IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURES, 0, -20, 128, 80);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(RegistryItems.FLUX_DUST.get()));
        this.timer = guiHelper.createTickTimer(60, 320, false);
    }

    @Nonnull
    public static List<CreatingFluxRecipe> getRecipes() {
        List<CreatingFluxRecipe> recipes = new ArrayList<>();
        recipes.add(new CreatingFluxRecipe(Blocks.BEDROCK, Blocks.OBSIDIAN,
                new ItemStack(Items.REDSTONE), new ItemStack(RegistryItems.FLUX_DUST.get())));
        recipes.add(new CreatingFluxRecipe(RegistryBlocks.FLUX_BLOCK.get(), Blocks.OBSIDIAN,
                new ItemStack(Items.REDSTONE), new ItemStack(RegistryItems.FLUX_DUST.get())));
        return recipes;
    }

    @Nonnull
    public static List<ItemStack> getCatalysts() {
        return List.of(new ItemStack(RegistryItems.FLUX_DUST.get()));
    }

    @Nonnull
    @Override
    public RecipeType<CreatingFluxRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return FluxTranslate.JEI_CREATING_FLUX.getComponent();
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull CreatingFluxRecipe recipe,
                          @Nonnull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8 + 1, 24 + 1)
                .addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102 + 1, 24 + 1)
                .addItemStack(recipe.output());
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, CreatingFluxRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        IRecipeCategory.super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);
        if (mouseX >= 40 && mouseX < 80 && mouseY >= 10 && mouseY < 64) {
            tooltip.addAll(
                List.of(
                    Component.literal("Y+2 = ").append(recipe.crusher().getName()),
                    Component.literal("Y+1 = ").append(recipe.input().getHoverName()),
                    Component.literal("Y+0 = ").append(recipe.base().getName())
                )
            );
        }
    }

    // 26.1 renders 3D blocks in a GUI only through picture-in-picture states, so the scene is drawn with flat icons.
    @Override
    public void draw(@Nonnull CreatingFluxRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView,
                     @Nonnull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);

        int value = timer.getValue();
        float offset = (value > 160 ? 160 - (value - 160) : value) / 10F;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, offset);
        guiGraphics.item(new ItemStack(recipe.crusher()), 56, 4);
        guiGraphics.pose().popMatrix();
        guiGraphics.item(value > 160 ? recipe.output() : recipe.input(), 56, 22);
        guiGraphics.item(new ItemStack(recipe.base()), 56, 38);

        Font fontRenderer = Minecraft.getInstance().font;
        String help = FluxTranslate.JEI_LEFT_CLICK.format(recipe.crusher().getName().getString());
        guiGraphics.text(fontRenderer, help, 64 - fontRenderer.width(help) / 2, 68, 0xff404040, false);
    }
}
