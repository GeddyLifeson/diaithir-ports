package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public record MicromissileClientTooltip(MicromissilesItem.Tooltip component) implements ClientTooltipComponent {
    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        if (component.stack().has(ModDataComponents.MICROMISSILE_SETTINGS)) {
            y += 3;
            @SuppressWarnings("ConstantConditions") int col = 0xFF000000 | ChatFormatting.GRAY.getColor();
            graphics.text(font, xlate("pneumaticcraft.gui.micromissile.topSpeed"), x, y, col, false);
            graphics.text(font, xlate("pneumaticcraft.gui.micromissile.turnSpeed"), x, y + font.lineHeight + 1, col, false);
            graphics.text(font, xlate("pneumaticcraft.gui.micromissile.damage"), x, y + font.lineHeight * 2 + 2, col, false);
        }
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        if (component.stack().has(ModDataComponents.MICROMISSILE_SETTINGS)) {
            MicromissilesItem.Settings settings = component.stack().get(ModDataComponents.MICROMISSILE_SETTINGS);
            y += 3;
            int vSpace = font.lineHeight + 1;
            int width = font.width(xlate("pneumaticcraft.gui.micromissile.topSpeed"));
            width = Math.max(width, font.width(xlate("pneumaticcraft.gui.micromissile.turnSpeed")));
            width = Math.max(width, font.width(xlate("pneumaticcraft.gui.micromissile.damage")));
            int barX = x + width + 5;
            int barW = getWidth(font) - width - 10;
            drawLine(graphics, barX, y, barW, settings.topSpeed());
            drawLine(graphics, barX, y + vSpace, barW, settings.turnSpeed());
            drawLine(graphics, barX, y + 2 * vSpace, barW, settings.damage());
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

    private static void drawLine(GuiGraphicsExtractor graphics, int x, int y, int totalWidth, float amount) {
        int w1 = (int)(totalWidth * amount);
        graphics.fill(x, y, x + totalWidth, y + 9, 0xFF181818);
        graphics.fill(x, y + 1, x + w1, y + 8, 0xFF00C000);
        for (int i = x + 3; i < x + w1; i += 4) {
            graphics.fill(i, y + 1, i + 1, y + 8, 0xFF006000);
        }
    }
}
