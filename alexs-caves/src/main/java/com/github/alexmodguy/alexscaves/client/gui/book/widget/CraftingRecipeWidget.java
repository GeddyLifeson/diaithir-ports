package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.gui.book.BookRecipes;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CraftingRecipeWidget extends BookWidget {

    @Expose
    @SerializedName("recipe_id")
    private String recipeId;
    @Expose
    private boolean sepia;

    private transient BookRecipes.Display recipe;
    private transient boolean loaded;

    private static final int GRID_TEXTURE_SIZE = 64;
    private static final Identifier CRAFTING_GRID_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/book/crafting_grid.png");
    private static final Identifier SMELTING_GRID_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/gui/book/smelting_grid.png");

    public CraftingRecipeWidget(int displayPage, String recipeId, boolean sepia, int x, int y, float scale) {
        super(displayPage, Type.CRAFTING_RECIPE, x, y, scale);
        this.recipeId = recipeId;
        this.sepia = sepia;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PageContext ctx, float partialTicks) {
        if (!loaded) {
            loaded = true;
            recipe = recipeId == null ? null : BookRecipes.get(recipeId);
        }
        if (recipe == null) {
            return;
        }
        var mc = Minecraft.getInstance();
        float ticks = mc.player == null ? 0 : mc.player.tickCount;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(getX(), getY());
        pose.scale(getScale(), getScale());
        // grid background: 55x37 texels drawn at 1.5x, centred
        pose.pushMatrix();
        pose.scale(1.5F, 1.5F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, recipe.smelting() ? SMELTING_GRID_TEXTURE : CRAFTING_GRID_TEXTURE, Math.round(-55 / 2F), Math.round(-37 / 2F), 0, 0, 55, 37, 55, 37, GRID_TEXTURE_SIZE, GRID_TEXTURE_SIZE, sepia ? SEPIA_TINT : -1);
        pose.popMatrix();
        if (recipe.smelting()) {
            ItemWidget.renderItem(graphics, recipe.result(), 43, -15, 1.35F * 1.25F);
            ItemWidget.renderItem(graphics, pick(recipe.cells().get(0), ticks), -27.5F, -12.5F, 1F);
        } else {
            ItemWidget.renderItem(graphics, recipe.result(), 57, 2, 1.35F * 1.25F);
            int width = recipe.width();
            List<List<ItemStack>> cells = recipe.cells();
            for (int i = 0; i < cells.size(); i++) {
                ItemStack stack = pick(cells.get(i), ticks);
                if (!stack.isEmpty()) {
                    ItemWidget.renderItem(graphics, stack, -33 + (i % width) * 18.75F, -18.5F + (i / width) * 19.5F, 1F);
                }
            }
        }
        pose.popMatrix();
    }

    private static ItemStack pick(List<ItemStack> options, float ticks) {
        if (options.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return options.get((int) ((ticks / 20F) % options.size()));
    }
}
