package com.lowdragmc.lowdraglib.gui.compat;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Optional;

/**
 * Thin compatibility layer exposing the pre-26.1 {@code GuiGraphics} drawing API on top of
 * {@link GuiGraphicsExtractor}, so the original PneumaticCraft GUI code can be ported with minimal changes.
 */
public final class GuiGraphics {
    private final GuiGraphicsExtractor g;
    private final Pose pose;
    private int tint = -1;

    private GuiGraphics(GuiGraphicsExtractor g) {
        this.g = g;
        this.pose = new Pose(g.pose());
    }

    public static GuiGraphics of(GuiGraphicsExtractor g) {
        return new GuiGraphics(g);
    }

    public GuiGraphicsExtractor raw() {
        return g;
    }

    public Pose pose() {
        return pose;
    }

    public int guiWidth() { return g.guiWidth(); }
    public int guiHeight() { return g.guiHeight(); }

    public void fill(int x0, int y0, int x1, int y1, int color) { g.fill(x0, y0, x1, y1, color); }
    public void fill(RenderPipeline p, int x0, int y0, int x1, int y1, int color) { g.fill(p, x0, y0, x1, y1, color); }
    public void fillGradient(int x0, int y0, int x1, int y1, int c1, int c2) { g.fillGradient(x0, y0, x1, y1, c1, c2); }
    public void hLine(int x0, int x1, int y, int color) { g.horizontalLine(x0, x1, y, color); }
    public void vLine(int x, int y0, int y1, int color) { g.verticalLine(x, y0, y1, color); }
    /** Draw a 1px line between two points (pixel stepping), like the old GL line primitives. */
    public void line(float x1, float y1, float x2, float y2, int color) {
        int steps = (int) Math.max(1, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)));
        for (int i = 0; i <= steps; i++) {
            int px = Math.round(x1 + (x2 - x1) * i / steps), py = Math.round(y1 + (y2 - y1) * i / steps);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }
    public void renderOutline(int x, int y, int w, int h, int color) { g.outline(x, y, w, h, color); }
    public void enableScissor(int x0, int y0, int x1, int y1) { g.enableScissor(x0, y0, x1, y1); }
    public void disableScissor() { g.disableScissor(); }

    public int drawString(Font font, String s, int x, int y, int color) { g.text(font, s, x, y, color); return x + font.width(s); }
    public int drawString(Font font, String s, int x, int y, int color, boolean shadow) { g.text(font, s, x, y, color, shadow); return x + font.width(s); }
    public int drawString(Font font, String s, float x, float y, int color, boolean shadow) { return drawString(font, s, (int) x, (int) y, color, shadow); }
    public int drawString(Font font, FormattedCharSequence s, float x, float y, int color, boolean shadow) { return drawString(font, s, (int) x, (int) y, color, shadow); }
    public int drawString(Font font, Component s, float x, float y, int color, boolean shadow) { return drawString(font, s, (int) x, (int) y, color, shadow); }
    public int drawString(Font font, Component s, int x, int y, int color) { g.text(font, s, x, y, color); return x + font.width(s); }
    public int drawString(Font font, Component s, int x, int y, int color, boolean shadow) { g.text(font, s, x, y, color, shadow); return x + font.width(s); }
    public int drawString(Font font, FormattedCharSequence s, int x, int y, int color) { g.text(font, s, x, y, color); return x + font.width(s); }
    public int drawString(Font font, FormattedCharSequence s, int x, int y, int color, boolean shadow) { g.text(font, s, x, y, color, shadow); return x + font.width(s); }
    public void drawCenteredString(Font font, String s, int x, int y, int color) { g.centeredText(font, s, x, y, color); }
    public void drawCenteredString(Font font, Component s, int x, int y, int color) { g.centeredText(font, s, x, y, color); }
    public void drawCenteredString(Font font, FormattedCharSequence s, int x, int y, int color) { g.centeredText(font, s, x, y, color); }

    /** Set a multiplicative tint (RGBA floats 0..1) for subsequent blits, like the old RenderSystem shader colour. */
    public void setColor(float r, float gr, float b, float a) {
        tint = (Math.round(a * 255) << 24) | (Math.round(r * 255) << 16) | (Math.round(gr * 255) << 8) | Math.round(b * 255);
    }
    public void setColor(int argb) { tint = argb; }
    public void resetColor() { tint = -1; }

    public void blit(Identifier tex, int x, int y, int u, int v, int w, int h) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, (float) u, (float) v, w, h, 256, 256, tint);
    }
    public void blit(Identifier tex, int x, int y, float u, float v, int w, int h, int texW, int texH) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, w, h, texW, texH, tint);
    }
    public void blit(Identifier tex, int x, int y, int w, int h, float u, float v, int uw, int vh, int texW, int texH) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, w, h, uw, vh, texW, texH, tint);
    }
    /** Draw a sprite from the blocks atlas (or any sprite), stretched into the given rect, tinted by ARGB colour, clipped to the rect. */
    public void blitSprite(net.minecraft.client.renderer.texture.TextureAtlasSprite sprite, int x, int y, int w, int h, int argb) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h, argb);
    }

    public void renderItem(ItemStack stack, int x, int y) { g.item(stack, x, y); }
    public void renderFakeItem(ItemStack stack, int x, int y) { g.fakeItem(stack, x, y); }
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) { g.itemDecorations(font, stack, x, y); }
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String text) { g.itemDecorations(font, stack, x, y, text); }

    public void renderTooltip(Font font, ItemStack stack, int x, int y) { g.setTooltipForNextFrame(font, stack, x, y); }
    public void renderTooltip(Font font, Component c, int x, int y) { g.setTooltipForNextFrame(font, c, x, y); }
    public void renderTooltip(Font font, List<? extends FormattedCharSequence> lines, int x, int y) { g.setTooltipForNextFrame(font, lines, x, y); }
    public void renderComponentTooltip(Font font, List<Component> lines, int x, int y) { g.setComponentTooltipForNextFrame(font, lines, x, y); }
    public void renderTooltip(Font font, List<Component> lines, Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> img, int x, int y) { g.setTooltipForNextFrame(font, lines, img, x, y); }

    /** Old-style 2D pose stack facade over the 26.1 {@link Matrix3x2fStack}. */
    public static final class Pose {
        private final Matrix3x2fStack m;
        Pose(Matrix3x2fStack m) { this.m = m; }
        public void pushPose() { m.pushMatrix(); }
        public void popPose() { m.popMatrix(); }
        public void translate(double x, double y, double z) { m.translate((float) x, (float) y); }
        public void translate(float x, float y, float z) { m.translate(x, y); }
        public void scale(float x, float y, float z) { m.scale(x, y); }
        public void mulPose(Quaternionf q) { m.rotate((float) (2 * Math.atan2(q.z, q.w))); }
        public Matrix3x2fStack matrix() { return m; }
    }
}
