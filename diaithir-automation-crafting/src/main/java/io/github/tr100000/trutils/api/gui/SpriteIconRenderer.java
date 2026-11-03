package io.github.tr100000.trutils.api.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class SpriteIconRenderer implements IconRenderer<SpriteIcon> {
    public static final SpriteIconRenderer INSTANCE = new SpriteIconRenderer();

    private SpriteIconRenderer() {}

    @Override
    public void draw(SpriteIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        drawWithSize(icon, graphics, x, y, 16);
    }

    @Override
    public void drawWithSize(SpriteIcon icon, GuiGraphicsExtractor graphics, int x, int y, int size) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon.location(), x - size / 2, y - size / 2, size, size);
    }
}
