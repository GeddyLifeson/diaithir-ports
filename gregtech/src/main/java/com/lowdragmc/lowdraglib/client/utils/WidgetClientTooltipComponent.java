package com.lowdragmc.lowdraglib.client.utils;

import com.lowdragmc.lowdraglib.gui.util.WidgetTooltipComponent;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/6/29
 * @implNote WidgetClientTooltipComponent
 */
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public record WidgetClientTooltipComponent(WidgetTooltipComponent tooltipComponent) implements ClientTooltipComponent {
    
    @Override
    public int getHeight(Font font) { return 0; }

    @Override
    public int getWidth(Font font) { return (int)0; /* ported stub */ }

    
    public void renderImage(Font textRenderer, int x, int y, GuiGraphics graphics) {  /* ported stub */ }
}
