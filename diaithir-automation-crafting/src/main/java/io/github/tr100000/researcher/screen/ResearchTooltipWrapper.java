package io.github.tr100000.researcher.screen;

import io.github.tr100000.researcher.Research;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ResearchTooltipWrapper {
    private final boolean showStatus;
    private @Nullable ResearchTooltipComponent tooltipComponent;

    public ResearchTooltipWrapper(boolean showStatus) {
        this.showStatus = showStatus;
    }

    public List<ClientTooltipComponent> getOrCreate(Research research) {
        if (tooltipComponent == null) {
            tooltipComponent = new ResearchTooltipComponent(research, showStatus);
        }
        return List.of(tooltipComponent);
    }
}
