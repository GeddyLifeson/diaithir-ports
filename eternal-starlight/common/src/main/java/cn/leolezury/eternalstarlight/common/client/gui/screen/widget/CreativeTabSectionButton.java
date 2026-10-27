package cn.leolezury.eternalstarlight.common.client.gui.screen.widget;

import net.minecraft.client.renderer.RenderPipelines;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.item.tab.ESCreativeModeTab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.Identifier;

public class CreativeTabSectionButton extends Button {
	public static final int SIZE = 24;
	public static final int SPACING = 28;
	public static final int COLUMN_SPACING = 28;
	public static final int BUTTONS_PER_COLUMN = 4;

	public static final Identifier TEXTURE = EternalStarlight.id("textures/gui/screen/creative_tab/section.png");
	public static final Identifier SELECTED_TEXTURE = EternalStarlight.id("textures/gui/screen/creative_tab/section_selected.png");
	private static final int TEXTURE_SIZE = 24;

	private final ESCreativeModeTab group;
	private final int sectionIndex;

	public CreativeTabSectionButton(int x, int y, ESCreativeModeTab group, int sectionIndex, Runnable onPress) {
		super(x, y, SIZE, SIZE, group.getSection(sectionIndex).name(), button -> onPress.run(), DEFAULT_NARRATION);
		this.group = group;
		this.sectionIndex = sectionIndex;
	}

	public boolean isSectionSelected() {
		return this.group.isSectionActive(this.sectionIndex);
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.isSectionSelected() ? SELECTED_TEXTURE : TEXTURE, getX(), getY(), (float) 0, (float) 0, SIZE, SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
		guiGraphics.item(this.group.getSection(this.sectionIndex).icon().get(), getX() + 4, getY() + 4);
	}
}
