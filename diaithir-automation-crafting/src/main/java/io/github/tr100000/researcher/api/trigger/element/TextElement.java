package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

public class TextElement implements TriggerDisplayElement {
    protected static final Minecraft client = Minecraft.getInstance();

    protected final FormattedCharSequence text;
    protected final int color;
    protected final boolean shadow;

    protected int cachedWidth = -1;

    public TextElement(FormattedCharSequence text, int color, boolean shadow) {
        this.text = text;
        this.color = color;
        this.shadow = shadow;
    }

    public TextElement(FormattedCharSequence text) {
        this(text, CommonColors.WHITE, true);
    }

    public TextElement(Component text, int color, boolean shadow) {
        this(text.getVisualOrderText(), color, shadow);
    }

    public TextElement(Component text) {
        this(text.getVisualOrderText());
    }

    @Override
    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        graphics.text(client.font, text, x, y + 4, color, shadow);
        return width();
    }

    @Override
    public int width() {
        if (cachedWidth < 0) cachedWidth = client.font.width(text);
        return cachedWidth;
    }
}
