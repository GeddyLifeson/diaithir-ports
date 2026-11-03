package io.github.tr100000.researcher.api.recipe;

import io.github.tr100000.trutils.api.gui.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.List;
import java.util.Objects;

public interface RecipeUnlockDisplay {
    RecipeUnlockDisplay ERROR = of(Icon.ERROR, ClientTooltipComponent.create(Component.literal("Error").getVisualOrderText()));

    Icon icon();
    List<ClientTooltipComponent> tooltip();

    record Impl(Icon icon, List<ClientTooltipComponent> tooltip) implements RecipeUnlockDisplay {}

    static RecipeUnlockDisplay of(Icon icon, ClientTooltipComponent tooltip) {
        return new Impl(icon, List.of(tooltip));
    }

    static RecipeUnlockDisplay of(Icon icon, ClientTooltipComponent... tooltip) {
        return new Impl(icon, List.of(tooltip));
    }

    static RecipeUnlockDisplay of(Icon icon, List<ClientTooltipComponent> tooltip) {
        return new Impl(icon, tooltip);
    }

    static ContextMap contextParameterMap() {
        return SlotDisplayContext.fromLevel(Objects.requireNonNull(Minecraft.getInstance().level));
    }
}
