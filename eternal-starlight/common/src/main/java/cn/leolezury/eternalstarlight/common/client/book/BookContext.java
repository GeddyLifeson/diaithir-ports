package cn.leolezury.eternalstarlight.common.client.book;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;

public interface BookContext {
	int getMouseX();

	int getMouseY();

	Font getFont();

	ResolvedBookDefinition getBookDefinition();

	int getContentX();

	int getContentY();

	int getTickCount();

	boolean isComponentEnabled(Identifier id);

	void jumpToComponent(Identifier id);
}
