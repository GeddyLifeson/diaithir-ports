package folk.sisby.antique_atlas.gui.core;

import folk.sisby.antique_atlas.util.Gfx;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;

/**
 * Displays a texture that changes alpha at regular intervals.
 * By default the texture file is assumed to be full image, but that behavior
 * can be altered by overriding the method {@link #drawImage(GuiGraphicsExtractor)}.
 *
 * @author Hunternif
 */
public class BlinkingTextureComponent extends GuiComponent {
	protected Identifier texture;
	/**
	 * The number of milliseconds the icon spends visible or invisible.
	 */
	protected final long blinkTime;
	protected final float visibleAlpha;
	protected final float invisibleAlpha;

	protected long lastTickTime;
	/**
	 * The flag that switches value every "blink".
	 */
	protected boolean isVisible;

	public BlinkingTextureComponent(long blinkTime, float visibleAlpha, float invisibleAlpha) {
		this.blinkTime = blinkTime;
		this.visibleAlpha = visibleAlpha;
		this.invisibleAlpha = invisibleAlpha;
	}

	public BlinkingTextureComponent() {
		this(500, 1, 0.25f);
	}

	public void setTexture(Identifier texture, int width, int height) {
		this.texture = texture;
		setSize(width, height);
		// Set up the timer so that the image appears visible at the first moment:
		lastTickTime = 0;
		isVisible = false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
		long currentTime = System.currentTimeMillis();
		if (lastTickTime + blinkTime < currentTime) {
			lastTickTime = currentTime;
			isVisible = !isVisible;
		}
		Gfx.tint(1, 1, 1, isVisible ? visibleAlpha : invisibleAlpha);

		drawImage(context);
		Gfx.tint(1, 1, 1, 1);
	}

	protected void drawImage(GuiGraphicsExtractor context) {
		Gfx.blit(context, texture, getGuiX(), getGuiY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
	}
}
