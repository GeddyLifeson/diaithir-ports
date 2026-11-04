package com.iafenvoy.jupiter.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class BadgeRenderer {
    public static void draw(GuiGraphicsExtractor extractor, Font font, int x, int y, Component text, int color) {
        x += 2;
        y += 2;
        drawFrame(extractor, x, y, font.width(text) + 3, font.lineHeight + 2, color);
        extractor.text(font, text, x + 2, y + 2, 0xFFFFFFFF);
    }

    public static void drawFrame(GuiGraphicsExtractor extractor, int x, int y, int width, int height, int color) {
        fill(extractor, x, y, x + width, y + height, color);
        fill(extractor, x, y, x - 1, y + height, color);
        fill(extractor, x + width, y, x + width + 1, y + height, color);
        fill(extractor, x, y, x + width, y - 1, color);
        fill(extractor, x, y + height, x + width, y + height + 1, color);
    }

    public static void fill(GuiGraphicsExtractor extractor, int minX, int minY, int maxX, int maxY, int color) {
        extractor.fill(minX, minY, maxX, maxY, color);
    }
}
