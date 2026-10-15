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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
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

    /**
     * 26.1: the in-GUI block renderer (BlockRenderDispatcher.renderSingleBlock with a buffer source) is gone, so the
     * block is drawn as its item icon; rotation, scale and z are ignored.
     */
    public static void renderBlockInGui(GuiGraphicsExtractor graphics, BlockState block, float x, float y, float z, float rotate, float scale) {
        graphics.item(new ItemStack(block.getBlock()), Math.round(x) - 8, Math.round(y) - 8);
    }

    /**
     * Draw a fluid texture, tiling as appropriate
     * @param graphics the pose stack
     * @param bounds bounds in which to draw
     * @param fluidStack the fluid to draw
     * @param tank a fluid tank; if non-null, fluid Y size is scaled according to the tank's capacity
     */
    public static void drawFluid(GuiGraphicsExtractor graphics, final Rect2i bounds, @Nullable FluidStack fluidStack, @Nullable IFluidTank tank) {
        if (fluidStack == null || fluidStack.getFluid() == Fluids.EMPTY) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        // 26.1: a fluid's sprite and tint live in its FluidModel
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite fluidStillSprite = model.stillMaterial().sprite();
        int tint = model.fluidTintSource() != null ? model.fluidTintSource().colorAsStack(fluidStack) : -1;

        int scaledAmount = tank == null ? bounds.getHeight() : fluidStack.getAmount() * bounds.getHeight() / tank.getCapacity();
        if (fluidStack.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        scaledAmount = Math.min(scaledAmount, bounds.getHeight());

        int yStart = bounds.getY() + bounds.getHeight();
        if (fluid.getFluidType().getDensity() < 0) yStart -= (bounds.getHeight() - scaledAmount);
        int yTop = yStart - scaledAmount;

        // tile 16x16 sprites up from the bottom, clipped to the fluid level
        graphics.enableScissor(bounds.getX(), yTop, bounds.getX() + bounds.getWidth(), yStart);
        for (int x = bounds.getX(); x < bounds.getX() + bounds.getWidth(); x += TEX_WIDTH) {
            for (int y = yStart - TEX_HEIGHT; y > yTop - TEX_HEIGHT; y -= TEX_HEIGHT) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, fluidStillSprite, x, y, TEX_WIDTH, TEX_HEIGHT, tint);
            }
        }
        graphics.disableScissor();
    }

    /** 26.1 text colours need an alpha byte; legacy RGB values (alpha 0) would draw nothing, so make them opaque. */
    public static int ensureAlpha(int color) {
        return (color >>> 24) == 0 ? color | 0xFF000000 : color;
    }

    public static Rect2i showPopupHelpScreen(GuiGraphicsExtractor graphics, Screen screen, Font fontRenderer, List<Component> helpText) {
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
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        drawPanel(graphics, 0, 0, boxHeight, boxWidth);

        int dy = 0;
        graphics.pose().scale(fontScale, fontScale);
        for (FormattedCharSequence line : l) {
            graphics.text(fontRenderer, line, 0, dy, 0xFFE0E0E0, false);
            dy += lineSpacing;
            if (maxLines-- == 0) break;
        }
        graphics.pose().popMatrix();

        return bounds;
    }

    public static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int panelHeight, int panelWidth) {
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y + panelHeight + 8, 0xC0000000);
        graphics.fill(x - 4, y - 4, x + panelWidth + 8, y - 3, 0xFF808080);
        graphics.fill(x - 4, y + panelHeight + 8, x + panelWidth + 8, y + panelHeight + 9, 0xFF808080);
        graphics.fill(x - 4, y - 4, x - 3, y + panelHeight + 8, 0xFF808080);
        graphics.fill(x + panelWidth + 8, y - 4, x + panelWidth + 9, y + panelHeight + 8, 0xFF808080);
    }

    public static void drawScaledText(GuiGraphicsExtractor graphics, Font fr, Component text, int x, int y, int color, float scale, boolean dropShadow) {
        color = ensureAlpha(color);
        if (scale != 1.0f) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.text(fr, text, 0, 0, color, dropShadow);
            graphics.pose().popMatrix();
        } else {
            graphics.text(fr, text, x, y, color, dropShadow);
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
     * @param color line's colour (ARGB)
     */
    public static void renderProgressingLine2d(GuiGraphicsExtractor graphics, ProgressingLine line, int color, float lineWidth) {
        float progress = line.getProgress();
        drawLine(graphics, line.startX, line.startY, lerp(progress, line.startX, line.endX), lerp(progress, line.startY, line.endY), lineWidth, color);
    }

    /**
     * Draw a straight line as a rotated filled rectangle. Replaces the old Tesselator/GL line drawing
     * (GuiUtils.drawWithTesselator no longer exists in 26.1).
     * @param width line thickness in GUI pixels
     * @param argb colour; the alpha byte must be set
     */
    public static void drawLine(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1, float width, int argb) {
        float dx = x1 - x0, dy = y1 - y0;
        float len = Mth.sqrt(dx * dx + dy * dy);
        if (len == 0) return;
        int half = Math.round(-width / 2f);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x0, y0);
        graphics.pose().rotate((float) Math.atan2(dy, dx));
        graphics.fill(0, half, Math.round(len), half + Math.max(1, Math.round(width)), argb);
        graphics.pose().popMatrix();
    }

    /**
     * Fill a triangle by scanline; a triangle fan is a series of these. Replaces Tesselator TRIANGLE_FAN drawing.
     * @param argb colour; the alpha byte must be set
     */
    public static void fillTriangle(GuiGraphicsExtractor graphics, float x0, float y0, float x1, float y1, float x2, float y2, int argb) {
        int yMin = (int) Math.floor(Math.min(y0, Math.min(y1, y2)));
        int yMax = (int) Math.ceil(Math.max(y0, Math.max(y1, y2)));
        float[] xs = { x0, x1, x2 };
        float[] ys = { y0, y1, y2 };
        for (int y = yMin; y < yMax; y++) {
            float yc = y + 0.5f;
            float lo = Float.MAX_VALUE, hi = -Float.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                int j = (i + 1) % 3;
                if ((ys[i] <= yc && ys[j] > yc) || (ys[j] <= yc && ys[i] > yc)) {
                    float x = xs[i] + (yc - ys[i]) / (ys[j] - ys[i]) * (xs[j] - xs[i]);
                    lo = Math.min(lo, x);
                    hi = Math.max(hi, x);
                }
            }
            if (lo <= hi) {
                int l = Math.round(lo);
                graphics.fill(l, y, Math.max(Math.round(hi), l + 1), y + 1, argb);
            }
        }
    }

    public static void drawPlayerSlots(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.GUI_SLOTS_9x3, x, y, 0, 0, 162, 54, 162, 54);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.GUI_SLOTS_9x1, x, y + 58, 0, 0, 162, 18, 162, 18);
    }

    public static void drawInsetPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        GuiUtils.drawNineSliced(graphics, Textures.GUI_INSET_PANEL, x, y, 0, width, height, 256, 128, 1);
    }

    /** z is ignored: the 26.1 GUI is 2D. */
    public static void drawNineSliced(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int z, int width, int height, int texWidth, int texHeight, int corner) {
        int centreWidth = texWidth - corner * 2;
        int centreHeight = texHeight - corner * 2;

        // corners
        quad(graphics, texture, x, y, 0, 0, corner, corner, texWidth, texHeight);
        quad(graphics, texture, x + width - corner, y, texWidth - corner, 0, corner, corner, texWidth, texHeight);
        quad(graphics, texture, x, y + height - corner, 0, texHeight - corner, corner, corner, texWidth, texHeight);
        quad(graphics, texture, x + width - corner, y + height - corner, texWidth - corner, texHeight - corner, corner, corner, texWidth, texHeight);

        // top & bottom edges
        for (int i = 0; i <= width / centreWidth; i++) {
            int x1 = corner + centreWidth * i;
            int w1 = Math.min(centreWidth, width - (i * centreWidth) - (corner * 2));
            quad(graphics, texture, x + x1, y, corner, 0, w1, corner, texWidth, texHeight);
            quad(graphics, texture, x + x1, y + height - corner, corner, texHeight - corner, w1, corner, texWidth, texHeight);
        }
        // left & right edges
        for (int i = 0; i <= height / centreHeight; i++) {
            int y1 = corner + centreHeight * i;
            int h1 = Math.min(centreHeight, height - (i * centreHeight) - (corner * 2));
            quad(graphics, texture, x, y + y1, 0, corner, corner, h1, texWidth, texHeight);
            quad(graphics, texture, x + width - corner, y + y1, texWidth - corner, corner, corner, h1, texWidth, texHeight);
        }

        // centre
        int cw = (width - (corner * 2)) / texWidth;
        int ch = (height - (corner * 2)) / texHeight;
        for (int ix = 0; ix <= cw; ix++) {
            for (int iy = 0; iy <= ch; iy++) {
                int x1 = corner + (ix * centreWidth);
                int y1 = corner + (iy * centreHeight);
                int w1 = Math.min(centreWidth, width - (ix * centreWidth) - (corner * 2));
                int h1 = Math.min(centreHeight, height - (iy * centreHeight) - (corner * 2));
                quad(graphics, texture, x + x1, y + y1, corner, corner, w1, h1, texWidth, texHeight);
            }
        }
    }

    private static void quad(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int texX, int texY, int w, int h, int texWidth, int texHeight) {
        if (w > 0 && h > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, texX, texY, w, h, w, h, texWidth, texHeight);
        }
    }

    public static void drawScreenWithTitleArea(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int titleColor) {
        GuiUtils.drawNineSliced(graphics, Textures.GUI_BLANK_256, x, y, 0, w, h, 256, 256, 4);
        graphics.horizontalLine(x + 3, x + w - 4, y + 15, 0xFF555555);
        graphics.horizontalLine(x + 3, x + w - 4, y + 16, 0xFFFFFFFF);
        graphics.fill(x + 3, y + 3, x + w - 3, y + 15, 0xFFDDD7BA);
    }
}
