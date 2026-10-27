package rpggods.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Small drawing helpers for the 256x256 textures used by the screens **/
public final class GuiUtil {
    private GuiUtil() {}

    /** Draws a region of a 256x256 texture **/
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width, int height) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, 256, 256);
    }

    /** Draws a region of a 256x256 texture with a color multiplier (ARGB) **/
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width, int height, int color) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, width, height, 256, 256, color);
    }

    /** Draws a region of a 256x256 texture stretched to the given size **/
    public static void blitStretched(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int srcWidth, int srcHeight,
                                     int dstWidth, int dstHeight) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, dstWidth, dstHeight, srcWidth, srcHeight, 256, 256);
    }
}
