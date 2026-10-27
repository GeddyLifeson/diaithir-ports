package cn.leolezury.eternalstarlight.common.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class ESGuiUtil {
	/**
	 * Draws a texture at a fractional position. GUI blits are integer based in 26.1, so the fraction is applied
	 * through the pose matrix and the quad is drawn at the origin.
	 */
	public static void blitFloat(GuiGraphicsExtractor graphics, Identifier resourceLocation, float x, float y, float width, float height, float texWidth, float texHeight) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(width / texWidth, height / texHeight);
		graphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, 0, 0, 0.0F, 0.0F, (int) texWidth, (int) texHeight, (int) texWidth, (int) texHeight);
		graphics.pose().popMatrix();
	}

	/**
	 * Replacement for the removed StringSplitter#componentStyleAtWidth.
	 */
	public static net.minecraft.network.chat.Style styleAtWidth(net.minecraft.client.gui.Font font, net.minecraft.util.FormattedCharSequence sequence, int width) {
		if (width < 0) {
			return null;
		}
		float[] accumulated = {0};
		net.minecraft.network.chat.Style[] found = {null};
		sequence.accept((index, style, codePoint) -> {
			accumulated[0] += font.width(net.minecraft.util.FormattedCharSequence.codepoint(codePoint, style));
			if (accumulated[0] > width) {
				found[0] = style;
				return false;
			}
			return true;
		});
		return found[0];
	}
}
