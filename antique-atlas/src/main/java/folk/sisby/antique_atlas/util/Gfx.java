package folk.sisby.antique_atlas.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Thin immediate-style layer over {@link GuiGraphicsExtractor} so the old Antique Atlas drawing code,
 * which set a shader colour and then drew textured quads, maps onto the 26.1 GUI extraction pipeline.
 * The tint is a render-thread global, reset by {@code tint(1, 1, 1, 1)}.
 */
public final class Gfx {
	private static int tint = 0xFFFFFFFF;

	private Gfx() {
	}

	public static void tint(float r, float g, float b, float a) {
		tint = ARGB.colorFromFloat(a, r, g, b);
	}

	public static void resetTint() {
		tint = 0xFFFFFFFF;
	}

	public static int currentTint() {
		return tint;
	}

	/** Draws a region of a texture at its natural size. */
	public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
		g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight, tint);
	}

	/** Draws a source region of srcWidth by srcHeight scaled to width by height. */
	public static void blitScaled(GuiGraphicsExtractor g, Identifier texture, int x, int y, int width, int height, int u, int v, int srcWidth, int srcHeight, int textureWidth, int textureHeight) {
		g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, srcWidth, srcHeight, textureWidth, textureHeight, tint);
	}

	public static void blitSprite(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int width, int height) {
		g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, tint);
	}

	public static void fill(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int argb) {
		g.fill(x0, y0, x1, y1, argb);
	}

	/** Draws a texture region with an explicit colour, ignoring the global tint. */
	public static void blitColored(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int argb) {
		g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight, argb);
	}

	/** Draws a whole texture centred on a point with a rotation in degrees and a scale. */
	public static void drawCenteredWithRotation(GuiGraphicsExtractor g, Identifier texture, double x, double y, float scale, int width, int height, float degrees, int argb) {
		g.pose().pushMatrix();
		g.pose().translate((float) x, (float) y);
		g.pose().rotate((float) Math.toRadians(degrees));
		g.pose().scale(scale, scale);
		g.blit(RenderPipelines.GUI_TEXTURED, texture, -width / 2, -height / 2, 0f, 0f, width, height, width, height, argb);
		g.pose().popMatrix();
	}
}
