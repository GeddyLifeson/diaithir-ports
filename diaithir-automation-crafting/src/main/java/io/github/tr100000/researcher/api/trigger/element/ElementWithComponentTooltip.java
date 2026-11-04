package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.trutils.api.gui.GuiHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;

import java.util.List;

public class ElementWithComponentTooltip extends AbstractElementWithTooltip {
    protected final List<ClientTooltipComponent> tooltip;

    public ElementWithComponentTooltip(TriggerDisplayElement element, List<ClientTooltipComponent> tooltip) {
        super(element);
        this.tooltip = tooltip;
    }

    public ElementWithComponentTooltip(TriggerDisplayElement element, ClientTooltipComponent... tooltip) {
        super(element);
        this.tooltip = List.of(tooltip);
    }

    public ElementWithComponentTooltip(TriggerDisplayElement element, ClientTooltipComponent tooltip) {
        super(element);
        this.tooltip = List.of(tooltip);
    }

    @Override
    protected void drawTooltip(final GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        GuiHelper.tooltip(graphics, client.font, tooltip, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
    }
}
