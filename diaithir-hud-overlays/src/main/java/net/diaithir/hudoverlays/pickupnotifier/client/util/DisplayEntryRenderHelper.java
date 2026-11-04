// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: item alpha mixins dropped (sprite hidden below a fade threshold); guava map replaced by plain thresholds.
package net.diaithir.hudoverlays.pickupnotifier.client.util;

import net.diaithir.hudoverlays.pickupnotifier.client.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

public final class DisplayEntryRenderHelper {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("tooltip/background");
    private static final Identifier FRAME_SPRITE = Identifier.withDefaultNamespace("tooltip/frame");
    private static final float ITEM_MIN_ALPHA = 0.35F;

    private DisplayEntryRenderHelper() {}

    public static void renderItem(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int x, int y, float alpha) {
        if (alpha >= ITEM_MIN_ALPHA) {
            guiGraphics.item(itemStack, x, y);
        }
    }

    private static Component shortenValue(int value) {
        if (value >= 1_000_000_000) {
            return Component.literal((value / 1_000_000_000) + "B");
        } else if (value >= 1_000_000) {
            return Component.literal((value / 1_000_000) + "M");
        } else if (value >= 1_000) {
            return Component.literal((value / 1_000) + "K");
        }
        return Component.literal(String.valueOf(value));
    }

    public static void renderGuiItemDecorations(GuiGraphicsExtractor guiGraphics, Font font, int count, int xPosition,
            int yPosition, float alpha) {
        if (count <= 1 && !ClientConfig.DISPLAY_SINGLE_COUNT.get()) {
            return;
        }
        guiGraphics.pose().pushMatrix();
        Component component = shortenValue(count);
        float scale = Math.min(1.0F, 16.0F / font.width(component));
        guiGraphics.pose().scale(scale, scale);
        float posX = (xPosition + 17) / scale - font.width(component);
        float posY = (yPosition + font.lineHeight * 2) / scale - font.lineHeight;
        guiGraphics.text(font, component, (int) posX, (int) posY, ARGB.color(alpha, -1), true);
        guiGraphics.pose().popMatrix();
    }

    public static void extractTooltipBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height,
            int color) {
        int x0 = x - 3 - 9;
        int y0 = y - 3 - 9;
        int paddedWidth = width + 3 + 3 + 18;
        int paddedHeight = height + 3 + 3 + 18;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, x0, y0, paddedWidth, paddedHeight, color);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, FRAME_SPRITE, x0, y0, paddedWidth, paddedHeight, color);
    }
}
