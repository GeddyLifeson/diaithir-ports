package net.diaithir.druidry.ars.api.client;

import net.minecraft.network.chat.Component;

import java.util.List;

/** Blocks/tiles that add lines to the wand/tooltip overlay. */
public interface ITooltipProvider {
    void getTooltip(List<Component> tooltip);

    default boolean shouldShowDetails() {
        return true;
    }
}
