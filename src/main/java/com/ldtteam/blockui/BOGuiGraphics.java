package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public class BOGuiGraphics
{
    private final GuiGraphicsExtractor guiGraphics;

    public BOGuiGraphics(final GuiGraphicsExtractor ms)
    {
        guiGraphics = ms;
    }

    public GuiGraphicsExtractor guiGraphics()
    {
        return guiGraphics;
    }

    public Matrix3x2fStack pose()
    {
        return guiGraphics.pose();
    }

    public int text(final String text, final int x, final int y, final int color, final boolean shadow)
    {
        final Font font = Minecraft.getInstance().font;
        guiGraphics.text(font, text, x, y, (color & 0xFF000000) == 0 ? color | 0xFF000000 : color, shadow);
        return x + font.width(text);
    }

    public void fill(final int x, final int y, final int width, final int height, final int color)
    {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    /**
     * Dropped in the 26.1.2 port: the 3D block-in-gui preview is now a flat item icon.
     *
     * @param data      blockState rendering data
     * @param itemStack backing itemStack for given blockState
     */
    public void renderBlockStateAsItem(final BlockStateRenderingData data, final ItemStack itemStack)
    {
        guiGraphics.item(itemStack, 0, 0);
    }

    public static double getAltSpeedFactor()
    {
        return Minecraft.getInstance().hasAltDown() ? 5 : 1;
    }
}
