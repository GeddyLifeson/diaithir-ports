package io.github.tr100000.researcher.api.trigger.element;

import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.trutils.api.gui.Icon;
import io.github.tr100000.trutils.api.gui.IconRenderers;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class IconElement implements TriggerDisplayElement {
    protected final Icon icon;

    public IconElement(Icon icon) {
        this.icon = icon;
    }

    @Override
    public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
        IconRenderers.draw(icon, graphics, x, y, delta);
        return 16;
    }

    @Override
    public int width() {
        return 16;
    }
}
