/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.util;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.gui.Font;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.util.Mth.lerp;

public class GuiUtils {
    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    public static final String TRANSLATION_LINE_BREAK = "\\n";

    public static void renderBlockInGui(GuiGraphics graphics, BlockState block, float x, float y, float z, float rotate, float scale) {
        // 26.1: block rendering in GUI is done through the item renderer; rotation is not supported
        net.minecraft.world.item.Item item = block.getBlock().asItem();
        if (item != net.minecraft.world.item.Items.AIR) {
            graphics.pose().pushPose();
            graphics.pose().translate(x - scale / 2, y - scale / 2, 0);
            graphics.pose().scale(scale / 16f, scale / 16f, 1f);
            graphics.renderItem(new net.minecraft.world.item.ItemStack(item), 0, 0);
            graphics.pose().popPose();
        }
    }

    /**
     * Draw a fluid texture, tiling as appropriate
     * @param graphics the pose stack
     * @param bounds bounds in which to draw
     * @param fluidStack the fluid to draw
     * @param tank a fluid tank; if non-null, fluid Y size is scaled according to the tank's capacity
     */
    public static void drawFluid(GuiGraphics graphics, final Rect2i bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == Fluids.EMPTY) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        Identifier fluidStill = me.desht.pneumaticcraft.client.PncFluidClient.stillSprite(fluid);
        if (fluidStill == null) {
            fluidStill = MissingTextureAtlasSprite.getLocation();
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(fluidStill);

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        final int xTileCount = bounds.getWidth() / TEX_WIDTH;
        final int xRemainder = bounds.getWidth() - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getFluidType().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);
        int color = me.desht.pneumaticcraft.client.PncFluidClient.tintColor(fluidStack);

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = bounds.getX() + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (bounds.getWidth() > 0 && h > 0 && w > 0) {
                    graphics.enableScissor(x, y + (TEX_HEIGHT - h), x + w, y + TEX_HEIGHT);
                    graphics.blitSprite(sprite, x, y, TEX_WIDTH, TEX_HEIGHT, color);
                    graphics.disableScissor();
                }
            }
        }
    }

    public static Rect2i showPopupHelpScreen(GuiGraphics graphics, Screen screen, Font fontRenderer, List<Component> helpText) {
        List<FormattedCharSequence> l = GuiUtils.wrapTextComponentList(helpText, screen.width / 2, fontRenderer);
        int lineSpacing = fontRenderer.lineHeight + 1;
        int boxHeight = l.size() * lineSpacing;
        int maxLines = boxHeight / lineSpacing;
        int boxWidth = l.stream().max(Comparator.comparingInt(fontRenderer::width)).map(fontRenderer::width).orElse(0);

        float fontScale = 1f;
        while (boxHeight > screen.height - 5) {
            boxHeight /= 2;
            boxWidth /= 2;
            fontScale /= 2;
        }

        int x, y;
        if (screen instanceof AbstractContainerScreen<?> a) {
            x = (a.getXSize() - boxWidth) / 2;
            y = (a.getYSize() - boxHeight) / 2;
        } else {
            x = (screen.width - boxWidth) / 2;
            y = (screen.height - boxHeight) / 2;
        }
        Rect2i bounds = new Rect2i(x, y, boxWidth, boxHeight);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 400);
        drawPanel(graphics, 0, 0, boxHeight, boxWidth);

        int dy = 0;
        graphics.pose().scale(fontScale, fontScale, fontScale);
        for (FormattedCharSequence line : l) {
            graphics.drawString(fontRenderer, line, 0, dy, 0xFFE0E0E0, false);
            dy += lineSpacing;
            if (maxLines-- == 0) break;
        }
        graphics.pose().popPose();

        return bounds;
    }

    public static void drawPanel(GuiGraphics graphics, int x, int y, int panelHeight, int panelWidth) {
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y + panelHeight + 8, 0xC0000000);
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y - 3, 0xFF808080);
        graphics.fill(x - 4, y + panelHeight + 8, x + panelWidth + 8, y + panelHeight + 9, 0xFF808080);
        graphics.fill(x - 4, y - 4, x - 3, y + panelHeight + 8, 0xFF808080);
        graphics.fill(x + panelWidth + 8, y - 4, x + panelWidth + 9, y + panelHeight + 8, 0xFF808080);
    }

    public static void drawScaledText(GuiGraphics graphics, Font fr, Component text, int x, int y, int color, float scale, boolean dropShadow) {
        if (scale != 1.0f) {
            var poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(x, y, 0);
            poseStack.scale(scale, scale, scale);
            graphics.drawString(fr, text, 0, 0, color, dropShadow);
            poseStack.popPose();
        } else {
            graphics.drawString(fr, text, x, y, color, dropShadow);
        }
    }

    public static List<FormattedCharSequence> wrapTextComponentList(List<Component> text, int maxWidth, Font font) {
        ImmutableList.Builder<FormattedCharSequence> builder = ImmutableList.builder();
        for (Component line : text) {
            builder.addAll(ComponentRenderUtils.wrapComponents(line, maxWidth, font));
        }
        return builder.build();
    }

    public static List<Component> xlateAndSplit(String key, Object... params) {
        return Arrays.stream(StringUtils.splitByWholeSeparator(Component.translatable(key, params).getString(), TRANSLATION_LINE_BREAK))
                .map(Component::literal)
                .collect(Collectors.toList());
    }

    /**
     * Render a progressing line in GUI context
     * @param graphics the matrix stack
     * @param line the line to render
     * @param color line's colour
     */
    public static void renderProgressingLine2d(GuiGraphics graphics, ProgressingLine line, int color, float lineWidth) {
        float progress = line.getProgress();
        float x1 = line.startX, y1 = line.startY;
        float x2 = lerp(progress, line.startX, line.endX), y2 = lerp(progress, line.startY, line.endY);
        // axis-aligned and diagonal lines are drawn as a run of 1px (lineWidth) squares
        int steps = (int) Math.max(1, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)));
        int w = Math.max(1, Math.round(lineWidth));
        for (int i = 0; i <= steps; i++) {
            int px = Math.round(x1 + (x2 - x1) * i / steps), py = Math.round(y1 + (y2 - y1) * i / steps);
            graphics.fill(px, py, px + w, py + w, color);
        }
    }

    public static void drawPlayerSlots(GuiGraphics graphics, int x, int y) {
        graphics.blit(Textures.GUI_SLOTS_9x3, x, y, 0, 0, 162, 54, 162, 54);
        graphics.blit(Textures.GUI_SLOTS_9x1, x, y + 58, 0, 0, 162, 18, 162, 18);
    }

    public static void drawInsetPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        GuiUtils.drawNineSliced(graphics, Textures.GUI_INSET_PANEL, x, y, 0, width, height, 256, 128, 1);
    }

    public static void drawNineSliced(GuiGraphics graphics, Identifier texture, int x, int y, int z, int width, int height, int texWidth, int texHeight, int corner) {
        int cw = texWidth - corner * 2, ch = texHeight - corner * 2;
        // corners
        graphics.blit(texture, x, y, 0, 0, corner, corner, 256, 256);
        graphics.blit(texture, x + width - corner, y, texWidth - corner, 0, corner, corner, 256, 256);
        graphics.blit(texture, x, y + height - corner, 0, texHeight - corner, corner, corner, 256, 256);
        graphics.blit(texture, x + width - corner, y + height - corner, texWidth - corner, texHeight - corner, corner, corner, 256, 256);
        // edges and centre, tiled
        for (int ix = corner; ix < width - corner; ix += cw) {
            int w = Math.min(cw, width - corner - ix);
            graphics.blit(texture, x + ix, y, corner, 0, w, corner, 256, 256);
            graphics.blit(texture, x + ix, y + height - corner, corner, texHeight - corner, w, corner, 256, 256);
            for (int iy = corner; iy < height - corner; iy += ch) {
                int h = Math.min(ch, height - corner - iy);
                graphics.blit(texture, x + ix, y + iy, corner, corner, w, h, 256, 256);
            }
        }
        for (int iy = corner; iy < height - corner; iy += ch) {
            int h = Math.min(ch, height - corner - iy);
            graphics.blit(texture, x, y + iy, 0, corner, corner, h, 256, 256);
            graphics.blit(texture, x + width - corner, y + iy, texWidth - corner, corner, corner, h, 256, 256);
        }
    }

    public static void drawScreenWithTitleArea(GuiGraphics graphics, int x, int y, int w, int h, int titleColor) {
        GuiUtils.drawNineSliced(graphics, Textures.GUI_BLANK_256, x, y, 0, w, h, 256, 256, 4);
        graphics.hLine(x + 3, x + w - 4, y + 15, 0xFF555555);
        graphics.hLine(x + 3, x + w - 4, y + 16, 0xFFFFFFFF);
        graphics.fill(x + 3, y + 3, x + w - 3, y + 15, 0xFFDDD7BA);
    }
}
