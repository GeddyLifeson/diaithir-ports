package io.github.tr100000.trutils.api.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class TextureIconRenderer implements IconRenderer<TextureIcon> {
    public static final TextureIconRenderer INSTANCE = new TextureIconRenderer();

    private TextureIconRenderer() {}

    @Override
    public void draw(TextureIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        drawWithSize(icon, graphics, x, y, 16);
    }

    @Override
    public void drawWithSize(TextureIcon icon, GuiGraphicsExtractor graphics, int x, int y, int size) {
        float scale = (float)size / icon.textureSize();
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture(), 0, 0, 0, 0, icon.textureSize(), icon.textureSize(), icon.textureSize(), icon.textureSize());
        graphics.pose().popMatrix();
    }
}
