package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

public record MicromissileClientTooltip(MicromissilesItem.Tooltip component) implements ClientTooltipComponent {
    @Override
    public void extractText(net.minecraft.client.gui.GuiGraphicsExtractor raw, Font pFont, int pX, int pY) {
        GuiGraphics graphics = GuiGraphics.of(raw);
        if (component.stack().has(ModDataComponents.MICROMISSILE_SETTINGS)) {
            pY += 3;
            @SuppressWarnings("ConstantConditions") int col = 0xFF000000 | ChatFormatting.GRAY.getColor();
            graphics.drawString(pFont, xlate("pneumaticcraft.gui.micromissile.topSpeed"), pX, pY, col, false);
            graphics.drawString(pFont, xlate("pneumaticcraft.gui.micromissile.turnSpeed"), pX, pY + pFont.lineHeight + 1, col, false);
            graphics.drawString(pFont, xlate("pneumaticcraft.gui.micromissile.damage"), pX, pY + pFont.lineHeight * 2 + 2, col, false);
        }
    }

    @Override
    public void extractImage(Font pFont, int pMouseX, int pMouseY, int w, int h, net.minecraft.client.gui.GuiGraphicsExtractor raw) {
        GuiGraphics graphics = GuiGraphics.of(raw);
        if (component.stack().has(ModDataComponents.MICROMISSILE_SETTINGS)) {
            MicromissilesItem.Settings settings = component.stack().get(ModDataComponents.MICROMISSILE_SETTINGS);
            pMouseY += 3;
            int vSpace = pFont.lineHeight + 1;
            int width = pFont.width(xlate("pneumaticcraft.gui.micromissile.topSpeed"));
            width = Math.max(width, pFont.width(xlate("pneumaticcraft.gui.micromissile.turnSpeed")));
            width = Math.max(width, pFont.width(xlate("pneumaticcraft.gui.micromissile.damage")));
            int barX = pMouseX + width + 5;
            int barY = pMouseY;
            int barW = getWidth(pFont) - width - 10;
            drawLine(graphics, barX, barY, barW, settings.topSpeed());
            drawLine(graphics, barX, barY + vSpace, barW, settings.turnSpeed());
            drawLine(graphics, barX, barY + 2 * vSpace, barW, settings.damage());
        }
    }

    @Override
    public int getHeight(Font font) {
        return component.stack().has(ModDataComponents.MICROMISSILE_SETTINGS) ? 33 : 0;
    }

    @Override
    public int getWidth(Font pFont) {
        return 150;
    }

    private static void drawLine(GuiGraphics graphics, int x, int y, int totalWidth, float amount) {
        int w1 = (int)(totalWidth * amount);
        graphics.fill(x, y, x + totalWidth, y + 9, 0xFF181818);
        graphics.fill(x, y + 1, x + w1, y + 8, 0xFF00C000);
        for (int i = x + 3; i < x + w1; i += 4) {
            graphics.fill(i, y + 1, i + 1, y + 8, 0xFF006000);
        }
    }
}
