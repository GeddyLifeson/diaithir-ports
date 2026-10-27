package cn.leolezury.eternalstarlight.common.client.gui.screen;

import net.minecraft.client.renderer.RenderPipelines;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.item.menu.AlloyFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class AlloyFurnaceScreen extends AbstractContainerScreen<AlloyFurnaceMenu> implements MenuAccess<AlloyFurnaceMenu> {
	private static final Identifier CONTAINER_BACKGROUND = EternalStarlight.id("textures/gui/screen/alloy_furnace/background.png");
	private static final Identifier LIT_PROGRESS = EternalStarlight.id("screen/alloy_furnace/lit_progress");
	private static final Identifier BURN_PROGRESS = EternalStarlight.id("screen/alloy_furnace/burn_progress");
	private static final Identifier COOLING_PROGRESS = EternalStarlight.id("screen/alloy_furnace/cooling_progress");
	private static final Identifier OVERHEAT_PROGRESS = EternalStarlight.id("screen/alloy_furnace/overheat_progress");

	public AlloyFurnaceScreen(AlloyFurnaceMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 166);
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, y, (float) 0, (float) 0, this.imageWidth, this.imageHeight, 256, 256);
		int litProgress = Mth.ceil(this.menu.getLitProgress() * 13.0F) + 1;
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS, 14, 14, 0, 14 - litProgress, this.leftPos + 10, this.topPos + 50 - litProgress, 14, litProgress);
		int burnProgress = Mth.ceil(this.menu.getBurnProgress() * 24.0F);
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS, 24, 16, 0, 0, this.leftPos + 89, this.topPos + 18, burnProgress, 16);
		int coolingProgress = Mth.ceil(this.menu.getCoolingProgress() * 15.0F) + 1;
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, COOLING_PROGRESS, 16, 16, 0, 16 - coolingProgress, this.leftPos + 85, this.topPos + 50 - coolingProgress, 16, coolingProgress);
		int overheatProgress = Mth.ceil(this.menu.getOverheatProgress() * 18.0F);
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, OVERHEAT_PROGRESS, 26, 18, 0, 18 - overheatProgress, this.leftPos + 89, this.topPos + 34 - overheatProgress, 26, overheatProgress);
	}
}