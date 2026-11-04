package io.github.tr100000.researcher.impl.recipe;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.trutils.api.gui.ExtendedTooltipComponent;
import io.github.tr100000.trutils.api.utils.GameUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.Collections;
import java.util.List;

public class CraftingRecipeTooltipComponent extends ExtendedTooltipComponent {
    private static final Minecraft client = Minecraft.getInstance();
    private static final Identifier BACKGROUND = ModUtils.id("textures/gui/crafting.png");

    private final int recipeWidth;
    private final List<List<ItemStack>> ingredients;
    private final ItemStack result;
    private final FormattedCharSequence recipeText;
    private final FormattedCharSequence idText;
    private final FormattedCharSequence modNameText;

    public static CraftingRecipeTooltipComponent create(Identifier recipeId, int recipeWidth, PlacementInfo ingredientPlacement, SlotDisplay result, ContextMap contextParameterMap) {
        List<List<ItemStack>> ingredients = new ObjectArrayList<>();
        for (int i = 0; i < ingredientPlacement.slotsToIngredientIndex().size(); i++) {
            int ingredientIndex = ingredientPlacement.slotsToIngredientIndex().getInt(i);
            if (ingredientIndex < 0) {
                ingredients.add(Collections.emptyList());
            }
            else {
                ingredients.add(ingredientPlacement.ingredients().get(ingredientIndex).display().resolveForStacks(contextParameterMap));
            }
        }

        ItemStack resultStack = result.resolveForFirstStack(contextParameterMap);

        return new CraftingRecipeTooltipComponent(recipeId, recipeWidth, ingredients, resultStack);
    }

    public CraftingRecipeTooltipComponent(Identifier recipeId, int recipeWidth, List<List<ItemStack>> ingredients, ItemStack result) {
        this.recipeWidth = recipeWidth;
        this.ingredients = ingredients;
        this.result = result;
        this.recipeText = Component.translatableWithFallback(recipeId.toLanguageKey("recipe"), result.getHoverName().getString()).getVisualOrderText();
        this.idText = Component.literal(recipeId.toString()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText();
        this.modNameText = Component.literal(GameUtils.getModName(recipeId.getNamespace())).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC).getVisualOrderText();
    }

    @Override
    public int getWidth(Font font) {
        return longestText(font, 176, recipeText, idText, modNameText);
    }

    @Override
    public int getHeight(Font font) {
        int textureHeight = 86;
        return 10 + textureHeight + 2 + (client.options.advancedItemTooltips ? 20 : 10);
    }

    @Override
    public void extractText(final GuiGraphicsExtractor graphics, Font font, int x, int y) {
        text(graphics, font, recipeText, x, y);
        y += 10;
        y += 86;
        y += 2;
        if (client.options.advancedItemTooltips) {
            text(graphics, font, idText, x, y);
            y += 10;
        }
        text(graphics, font, modNameText, x, y);
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, final GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y + 10, 0, 0, width, height, 256, 256);

        final long currentTick = System.currentTimeMillis() / 1000;

        for (int i = 0; i < ingredients.size(); i++) {
            List<ItemStack> stacks = ingredients.get(i);
            if (stacks.isEmpty()) continue;
            int row = i / recipeWidth;
            int column = i % recipeWidth;

            ItemStack stack = stacks.get((int)(currentTick % stacks.size()));
            graphics.fakeItem(stack, x + 30 + column * 18, y + 27 + row * 18);
        }

        graphics.fakeItem(result, x + 124, y + 45);
    }
}
