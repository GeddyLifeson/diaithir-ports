package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class EmptyElement implements TriggerDisplayElement {
    private EmptyElement() {}

    public static final EmptyElement INSTANCE = new EmptyElement();

    @Override
    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        return 0;
    }

    @Override
    public int width() {
        return 0;
    }
}
