package net.diaithir.gear.toolbelt.client.radial;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public interface IDrawingHelper
{
    void renderTooltip(GuiGraphicsExtractor graphics, ItemStack stack, int mouseX, int mouseY);
}
