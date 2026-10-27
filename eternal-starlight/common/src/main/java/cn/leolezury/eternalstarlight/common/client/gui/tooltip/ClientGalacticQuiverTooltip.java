package cn.leolezury.eternalstarlight.common.client.gui.tooltip;

import net.minecraft.client.renderer.RenderPipelines;
import cn.leolezury.eternalstarlight.common.item.component.LargeItemStackList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ClientGalacticQuiverTooltip implements ClientTooltipComponent {
	private static final Identifier SLOT = Identifier.withDefaultNamespace("container/slot");
	private final List<LargeItemStackList.LargeItemStack> contents;

	public ClientGalacticQuiverTooltip(List<LargeItemStackList.LargeItemStack> contents) {
		this.contents = contents;
	}

	@Override
	public int getHeight(Font font) {
		return this.gridSizeY() * 18 + 6;
	}

	@Override
	public int getWidth(Font font) {
		return this.gridSizeX() * 18 + 2;
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
		int sizeX = this.gridSizeX();
		int sizeY = this.gridSizeY();
		int index = 0;

		for (int gridX = 0; gridX < sizeY; gridX++) {
			for (int gridY = 0; gridY < sizeX; gridY++) {
				this.renderSlot(x + gridY * 18 + 1, y + gridX * 18 + 1, index++, guiGraphics, font);
			}
		}
	}

	private void renderSlot(int x, int y, int itemIndex, GuiGraphicsExtractor guiGraphics, Font font) {
		if (itemIndex < this.contents.size()) {
			LargeItemStackList.LargeItemStack stack = this.contents.get(itemIndex);
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, x, y, 18, 18);
			guiGraphics.item(stack.getItem(), x + 1, y + 1, itemIndex);
			guiGraphics.itemDecorations(font, stack.asItemStack(), x + 1, y + 1);
		}
	}

	private int gridSizeX() {
		return Math.max(2, (int) Math.ceil(Math.sqrt((double) this.contents.size() + 1.0)));
	}

	private int gridSizeY() {
		return (int) Math.ceil(((double) this.contents.size() + 1.0) / (double) this.gridSizeX());
	}
}
