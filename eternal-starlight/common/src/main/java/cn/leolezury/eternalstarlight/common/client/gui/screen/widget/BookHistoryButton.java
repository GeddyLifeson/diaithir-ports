package cn.leolezury.eternalstarlight.common.client.gui.screen.widget;

import net.minecraft.client.renderer.RenderPipelines;
import cn.leolezury.eternalstarlight.common.client.book.BookDefinition;
import cn.leolezury.eternalstarlight.common.client.gui.screen.BookScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;

public class BookHistoryButton extends Button {
	private final BookDefinition book;
	private final boolean isLeft;

	public BookHistoryButton(int x, int y, BookDefinition book, boolean isLeft, OnPress onPress) {
		super(x, y, book.buttons().leftRightButtonWidth(), book.buttons().leftRightButtonHeight(), CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		this.book = book;
		this.isLeft = isLeft;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, isLeft ? book.textures().leftButton() : book.textures().rightButton(), this.getX(), this.getY(), (float) 0, (float) 0, book.buttons().leftRightButtonWidth(), book.buttons().leftRightButtonHeight(), book.buttons().leftRightButtonWidth(), book.buttons().leftRightButtonHeight());
		guiGraphics.pose().popMatrix();
	}
}
