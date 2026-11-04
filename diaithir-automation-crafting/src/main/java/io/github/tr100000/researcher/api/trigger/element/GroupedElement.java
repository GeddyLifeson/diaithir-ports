package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class GroupedElement implements TriggerDisplayElement {
    protected final List<TriggerDisplayElement> elements;

    public GroupedElement(List<TriggerDisplayElement> elements) {
        this.elements = elements;
    }

    public GroupedElement(TriggerDisplayElement... elements) {
        this.elements = List.of(elements);
    }

    @Override
    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        int totalWidth = 0;

        for (TriggerDisplayElement element : elements) {
            totalWidth += element.extractRenderState(graphics, x + totalWidth, y, mouseX, mouseY, delta);
        }

        return totalWidth;
    }

    @Override
    public int width() {
        int totalWidth = 0;

        for (TriggerDisplayElement element : elements) {
            totalWidth += element.width();
        }

        return totalWidth;
    }
}
