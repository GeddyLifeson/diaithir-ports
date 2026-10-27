package com.diaithir.addons.newage.content;

import java.util.List;

import net.minecraft.network.chat.Component;

/** Blocks that contribute lines to their item's tooltip (client-side only call). */
public interface TooltipBlock {
    void appendTooltip(List<Component> tooltipComponents);
}
