package vectorwing.farmersdelight.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// ponytail: the in-pot recipe book is gone (26.1 rebuilt the recipe book API); JEI shows the cooking pot recipes
public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu>
{
	private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "textures/gui/cooking_pot.png");
	private static final Rectangle HEAT_ICON = new Rectangle(47, 55, 17, 15);
	private static final Rectangle PROGRESS_ARROW = new Rectangle(89, 25, 0, 17);
	private static final int MEAL_SLOT = 6;

	public CookingPotScreen(CookingPotMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = 28;
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
		super.extractTooltip(gui, mouseX, mouseY);
		if (this.isHovering(HEAT_ICON.x, HEAT_ICON.y, HEAT_ICON.width, HEAT_ICON.height, mouseX, mouseY)) {
			gui.setTooltipForNextFrame(TextUtils.container("cooking_pot." + (this.menu.isHeated() ? "heated" : "not_heated")), mouseX, mouseY);
		}
	}

	/** The meal slot shows the meal's name and the container it is served in, not the full item tooltip. */
	@Override
	protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
		if (this.hoveredSlot == null || this.hoveredSlot.index != MEAL_SLOT) {
			return super.getTooltipFromContainerItem(stack);
		}
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(stack.getItem().getName(stack).copy().withStyle(stack.getRarity().getStyleModifier()));
		ItemStack containerStack = this.menu.blockEntity.getContainer();
		if (!containerStack.isEmpty()) {
			tooltip.add(TextUtils.container("cooking_pot.served_on", containerStack.getHoverName().getString()).withStyle(ChatFormatting.GRAY));
		}
		return tooltip;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(gui, mouseX, mouseY, partialTicks);
		gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		if (this.menu.isHeated()) {
			gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + HEAT_ICON.x, this.topPos + HEAT_ICON.y, 176, 0, HEAT_ICON.width, HEAT_ICON.height, 256, 256);
		}
		int progress = this.menu.getCookProgressionScaled();
		gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + PROGRESS_ARROW.x, this.topPos + PROGRESS_ARROW.y, 176, 15, progress + 1, PROGRESS_ARROW.height, 256, 256);
	}
}
