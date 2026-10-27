package cn.leolezury.eternalstarlight.common.client.gui.screen;

import net.minecraft.client.renderer.RenderPipelines;
import cn.leolezury.eternalstarlight.common.item.menu.CrateMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CrateScreen extends AbstractContainerScreen<CrateMenu> implements MenuAccess<CrateMenu> {
	private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

	public CrateScreen(CrateMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 114 + 4 * 18);
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, y, (float) 0, (float) 0, this.imageWidth, 4 * 18 + 17, 256, 256);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, y + 4 * 18 + 17, (float) 0, (float) 126, this.imageWidth, 96, 256, 256);
	}
}