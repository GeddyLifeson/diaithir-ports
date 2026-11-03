package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.util.List;

public class TimedSwitchingElement implements TriggerDisplayElement {
    protected final List<TriggerDisplayElement> elements;
    protected final float switchTimeSeconds;

    public TimedSwitchingElement(float switchTimeSeconds, List<TriggerDisplayElement> elements) {
        this.elements = elements;
        this.switchTimeSeconds = switchTimeSeconds;
    }

    public TimedSwitchingElement(float switchTimeSeconds, TriggerDisplayElement... elements) {
        this(switchTimeSeconds, List.of(elements));
    }

    public TimedSwitchingElement(List<TriggerDisplayElement> elements) {
        this(1.0F, elements);
    }

    public TimedSwitchingElement(TriggerDisplayElement... elements) {
        this(List.of(elements));
    }

    private TriggerDisplayElement getCurrentElement() {
        final long timeMilliseconds = Util.getMillis();
        int index = Mth.floor((double)timeMilliseconds / 1000L / switchTimeSeconds) % elements.size();
        return elements.get(index);
    }

    @Override
    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        return getCurrentElement().extractRenderState(graphics, x, y, mouseX, mouseY, delta);
    }

    @Override
    public int width() {
        return getCurrentElement().width();
    }
}
