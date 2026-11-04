package io.github.tr100000.researcher.api.reward;

import io.github.tr100000.trutils.api.gui.Icon;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ResearchClientReward {
    ResearchClientReward ERROR = of(Icon.ERROR, ClientTooltipComponent.create(Component.literal("Error").getVisualOrderText()));

    Icon icon();
    List<ClientTooltipComponent> tooltip();

    record Impl(Icon icon, List<ClientTooltipComponent> tooltip) implements ResearchClientReward {}

    static ResearchClientReward of(Icon icon, ClientTooltipComponent tooltip) {
        return of(icon, List.of(tooltip));
    }

    static ResearchClientReward of(Icon icon, ClientTooltipComponent... tooltip) {
        return of(icon, List.of(tooltip));
    }

    static ResearchClientReward of(Icon icon, List<ClientTooltipComponent> tooltip) {
        return new Impl(icon, tooltip);
    }
}
