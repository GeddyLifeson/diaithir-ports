package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.trutils.api.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class AbstractElementWithTooltip implements TriggerDisplayElement {
    protected static final Minecraft client = Minecraft.getInstance();

    protected final TriggerDisplayElement element;

    protected AbstractElementWithTooltip(TriggerDisplayElement element) {
        if (element instanceof AbstractElementWithTooltip elementWithTooltip) {
            element = elementWithTooltip.element;
        }
        this.element = element;
    }

    protected abstract void drawTooltip(final GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta);

    @Override
    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        int width = element.extractRenderState(graphics, x, y, mouseX, mouseY, delta);

        if (GuiHelper.isMouseTouching(x, y, width, 16, mouseX, mouseY)) {
            drawTooltip(graphics, x, y, mouseX, mouseY, delta);
        }

        return width;
    }

    @Override
    public int width() {
        return element.width();
    }
}
