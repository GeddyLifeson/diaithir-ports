package io.github.tr100000.researcher.api.trigger;

import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.api.trigger.element.GroupedElement;
import io.github.tr100000.researcher.api.trigger.element.SpacingElement;
import io.github.tr100000.researcher.api.trigger.element.TextElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class TriggerDisplay implements TriggerDisplayElement {
    private static final int EDGE_PADDING = 2;

    private final List<TriggerDisplayElement> elements;

    public TriggerDisplay(List<TriggerDisplayElement> elements) {
        this.elements = elements;
    }

    public TriggerDisplay(TriggerDisplayElement... elements) {
        this(List.of(elements));
    }

    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        int totalWidth = EDGE_PADDING;
        for (TriggerDisplayElement element : elements) {
            element.extractRenderState(graphics, x + totalWidth, y, mouseX, mouseY, delta);
            totalWidth += element.width();
        }
        return totalWidth + EDGE_PADDING;
    }

    public int width() {
        int totalWidth = EDGE_PADDING;
        for (TriggerDisplayElement element : elements) {
            totalWidth += element.width();
        }
        return totalWidth + EDGE_PADDING;
    }

    @Contract(value = "_ -> new", pure = true)
    public static TriggerDisplayElement makeCountElement(ResearchCriterion<?> criterion) {
        return new GroupedElement(
                new TextElement(Component.literal(String.valueOf(criterion.count()))),
                new SpacingElement(2),
                new TextElement(Component.literal("×")),
                new TextElement(Component.literal(" "))
        );
    }
}
