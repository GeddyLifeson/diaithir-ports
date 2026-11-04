package io.github.tr100000.researcher.api.trigger;

import io.github.tr100000.researcher.api.trigger.element.ElementWithComponentTooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.List;

public interface TriggerDisplayElement {
    int extractRenderState(final GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta);

    @Contract(pure = true)
    int width();

    @Contract(value = "_ -> new", pure = true)
    default TriggerDisplayElement withTooltip(ClientTooltipComponent tooltip) {
        return new ElementWithComponentTooltip(this, tooltip);
    }

    @Contract(value = "_ -> new", pure = true)
    default TriggerDisplayElement withTooltip(ClientTooltipComponent... tooltip) {
        return new ElementWithComponentTooltip(this, tooltip);
    }

    @Contract(value = "_ -> new", pure = true)
    default TriggerDisplayElement withTooltip(List<ClientTooltipComponent> tooltip) {
        return new ElementWithComponentTooltip(this, tooltip);
    }

    @Contract(value = "_ -> new", pure = true)
    default TriggerDisplayElement withTextTooltip(List<? extends Component> text) {
        return withTooltip(text.stream().map(t -> ClientTooltipComponent.create(t.getVisualOrderText())).toList());
    }

    @Contract(value = "_ -> new", pure = true)
    default TriggerDisplayElement withTextTooltip(Component... text) {
        return withTooltip(Arrays.stream(text).map(t -> ClientTooltipComponent.create(t.getVisualOrderText())).toList());
    }
}
