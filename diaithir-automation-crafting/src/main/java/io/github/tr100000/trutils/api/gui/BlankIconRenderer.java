package io.github.tr100000.trutils.api.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BlankIconRenderer implements IconRenderer<BlankIcon> {
    public static final BlankIconRenderer INSTANCE = new BlankIconRenderer();

    @Override
    public void draw(BlankIcon icon, GuiGraphicsExtractor graphics, int x, int y) { /* do nothing */ }

    @Override
    public void drawWithSize(BlankIcon icon, GuiGraphicsExtractor graphics, int x, int y, int size) { /* do nothing */ }
}
