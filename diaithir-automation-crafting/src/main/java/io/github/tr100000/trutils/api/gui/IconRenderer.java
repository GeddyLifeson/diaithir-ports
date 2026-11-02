package io.github.tr100000.trutils.api.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IconRenderer<T extends Icon> {
    default void draw(T icon, GuiGraphicsExtractor graphics, int x, int y) {
        draw(icon, graphics, x, y, 0);
    }

    default void draw(T icon, GuiGraphicsExtractor graphics, int x, int y, float delta) {
        draw(icon, graphics, x, y);
    }

    default void drawWithSize(T icon, GuiGraphicsExtractor graphics, int x, int y, int size) {
        drawWithSize(icon, graphics, x, y, size, 0);
    }

    default void drawWithSize(T icon, GuiGraphicsExtractor graphics, int x, int y, int size, float delta) {
        drawWithSize(icon, graphics, x, y, size);
    }
}
