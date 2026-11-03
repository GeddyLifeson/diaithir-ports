package io.github.tr100000.researcher.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ResearchDescriptionWidget extends AbstractWidget {
    private final Font font;
    private final List<FormattedCharSequence> wrappedText;

    public ResearchDescriptionWidget(int x, int y, int width, Component text, Font font) {
        super(x, y, width, 0, text);
        this.font = font;
        this.wrappedText = font.split(text, width - 10);
        this.height = wrappedText.size() * 9;
    }

    @Override
    public void extractWidgetRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (getMessage().getString().isBlank()) return;

        for (int i = 0; i < wrappedText.size(); i++) {
            graphics.text(font, wrappedText.get(i), getX(), getY() + 9 * i, CommonColors.WHITE);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, getMessage());
    }
}
