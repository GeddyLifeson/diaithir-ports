package com.lowdragmc.lowdraglib.gui.util;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Rect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * 26.1 port of the LDLib drawing helpers. The shader based rounded shapes of the original are approximated with
 * plain rectangles drawn through {@link GuiGraphics}.
 */
@OnlyIn(Dist.CLIENT)
public class DrawerHelper {

    public static void init() {}

    public static void drawFluidTexture(@Nonnull GuiGraphics graphics, float xCoord, float yCoord,
                                        TextureAtlasSprite textureSprite, float maskTop, float maskRight,
                                        float zLevel, int fluidColor) {
        int w = Math.round(16 - maskRight), h = Math.round(16 - maskTop);
        if (w <= 0 || h <= 0) return;
        graphics.enableScissor((int) xCoord, (int) (yCoord + maskTop), (int) xCoord + w, (int) yCoord + 16);
        graphics.blitSprite(textureSprite, (int) xCoord, (int) yCoord, 16, 16, fluidColor);
        graphics.disableScissor();
    }

    public static void drawFluidForGui(@Nonnull GuiGraphics graphics, FluidStack contents, long tankCapacity,
                                       int startX, int startY, int widthT, int heightT) {
        drawFluidForGui(graphics, contents, (float) startX, (float) startY, (float) widthT, (float) heightT);
    }

    public static void drawFluidForGui(@Nonnull GuiGraphics graphics, FluidStack contents, float startX, float startY,
                                       float widthT, float heightT) {
        TextureAtlasSprite fluidStillSprite = FluidHelper.getStillTexture(contents);
        if (fluidStillSprite == null) {
            if (Platform.isDevEnv()) {
                LDLib.LOGGER.error("Missing fluid texture for fluid: " + contents.getHoverName().getString());
            }
            return;
        }
        int fluidColor = FluidHelper.getColor(contents) | 0xff000000;
        final int xTileCount = (int) (widthT / 16);
        final float xRemainder = widthT - xTileCount * 16;
        final int yTileCount = (int) (heightT / 16);
        final float yRemainder = heightT - yTileCount * 16;
        final float yStart = startY + heightT;
        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                float width = xTile == xTileCount ? xRemainder : 16;
                float height = yTile == yTileCount ? yRemainder : 16;
                float x = startX + xTile * 16;
                float y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    drawFluidTexture(graphics, x, y, fluidStillSprite, 16 - height, 16 - width, 0, fluidColor);
                }
            }
        }
    }

    public static void drawBorder(@Nonnull GuiGraphics graphics, int x, int y, int width, int height, int color,
                                  int border) {
        drawSolidRect(graphics, x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(graphics, x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(graphics, x - border, y, border, height, color);
        drawSolidRect(graphics, x + width, y, border, height, color);
    }

    public static void drawStringSized(@Nonnull GuiGraphics graphics, String text, float x, float y, int color,
                                       boolean dropShadow, float scale, boolean center) {
        graphics.pose().pushPose();
        Font fontRenderer = Minecraft.getInstance().font;
        double scaledTextWidth = center ? fontRenderer.width(text) * scale : 0.0;
        graphics.pose().translate(x - scaledTextWidth / 2.0, y, 0.0f);
        graphics.pose().scale(scale, scale, scale);
        graphics.drawString(fontRenderer, text, 0, 0, color, dropShadow);
        graphics.pose().popPose();
    }

    public static void drawStringFixedCorner(@Nonnull GuiGraphics graphics, String text, float x, float y, int color,
                                             boolean dropShadow, float scale) {
        Font fontRenderer = Minecraft.getInstance().font;
        float scaledWidth = fontRenderer.width(text) * scale;
        float scaledHeight = fontRenderer.lineHeight * scale;
        drawStringSized(graphics, text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    public static void drawText(@Nonnull GuiGraphics graphics, String text, float x, float y, float scale, int color) {
        drawText(graphics, text, x, y, scale, color, false);
    }

    public static void drawText(@Nonnull GuiGraphics graphics, String text, float x, float y, float scale, int color,
                                boolean shadow) {
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1f);
        float sf = 1 / scale;
        graphics.drawString(fontRenderer, text, (int) (x * sf), (int) (y * sf), color, shadow);
        graphics.pose().popPose();
    }

    public static void drawItemStack(@Nonnull GuiGraphics graphics, ItemStack itemStack, int x, int y, int color,
                                     @Nullable String altTxt) {
        Minecraft mc = Minecraft.getInstance();
        graphics.renderItem(itemStack, x, y);
        if (altTxt != null) graphics.renderItemDecorations(mc.font, itemStack, x, y, altTxt);
        else graphics.renderItemDecorations(mc.font, itemStack, x, y);
    }

    public static List<Component> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getInstance();
        return Screen.getTooltipFromItem(mc, itemStack);
    }

    public static void drawSolidRect(@Nonnull GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public static void drawSolidRect(@Nonnull GuiGraphics graphics, Rect rect, int color) {
        drawSolidRect(graphics, rect.left, rect.up, rect.right - rect.left, rect.down - rect.up, color);
    }

    public static void drawRectShadow(@Nonnull GuiGraphics graphics, int x, int y, int width, int height,
                                      int distance) {
        drawGradientRect(graphics, x + distance, y + height, width - distance, distance, 0x4f000000, 0, false);
        drawGradientRect(graphics, x + width, y + distance, distance, height - distance, 0x4f000000, 0, true);
    }

    public static void drawGradientRect(@Nonnull GuiGraphics graphics, int x, int y, int width, int height,
                                        int startColor, int endColor) {
        drawGradientRect(graphics, x, y, width, height, startColor, endColor, false);
    }

    public static void drawGradientRect(@Nonnull GuiGraphics graphics, float x, float y, float width, float height,
                                        int startColor, int endColor, boolean horizontal) {
        if (horizontal) {
            int steps = Math.max(1, Math.min((int) width, 32));
            for (int i = 0; i < steps; i++) {
                float t = (i + 0.5f) / steps;
                graphics.fill((int) (x + width * i / steps), (int) y, (int) (x + width * (i + 1) / steps),
                        (int) (y + height), lerp(startColor, endColor, t));
            }
        } else {
            graphics.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), startColor, endColor);
        }
    }

    private static int lerp(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void drawLines(@Nonnull GuiGraphics graphics, List<Vec2> points, int startColor, int endColor,
                                 float width) {
        for (int i = 0; i + 1 < points.size(); i++) {
            Vec2 a = points.get(i), b = points.get(i + 1);
            graphics.line(a.x, a.y, b.x, b.y, lerp(startColor, endColor, i / (float) Math.max(1, points.size() - 1)));
        }
    }

    public static void drawTextureRect(@Nonnull GuiGraphics graphics, float x, float y, float width, float height) {}

    public static void drawRound(@Nonnull GuiGraphics graphics, int color, float radius, Position centerPos) {
        graphics.fill((int) (centerPos.x - radius), (int) (centerPos.y - radius), (int) (centerPos.x + radius),
                (int) (centerPos.y + radius), color);
    }

    public static void drawPanelBg(@Nonnull GuiGraphics graphics) {
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xF2141414);
    }

    public static void drawRoundBox(@Nonnull GuiGraphics graphics, Rect square, Vector4f radius, int color) {
        graphics.fill(square.left, square.up, square.right, square.down, color);
    }

    public static void drawProgressRoundBox(@Nonnull GuiGraphics graphics, Rect square, Vector4f radius, int color1,
                                            int color2, float progress) {
        int w = square.right - square.left;
        graphics.fill(square.left, square.up, square.right, square.down, color2);
        graphics.fill(square.left, square.up, square.left + (int) (w * progress), square.down, color1);
    }

    public static void drawFrameRoundBox(@Nonnull GuiGraphics graphics, Rect square, float thickness,
                                         Vector4f radius1, Vector4f radius2, int color) {
        int t = Math.max(1, Math.round(thickness));
        int w = square.right - square.left, h = square.down - square.up;
        graphics.fill(square.left, square.up, square.right, square.up + t, color);
        graphics.fill(square.left, square.down - t, square.right, square.down, color);
        graphics.fill(square.left, square.up, square.left + t, square.down, color);
        graphics.fill(square.right - t, square.up, square.right, square.down, color);
    }

    public static void drawRoundLine(@Nonnull GuiGraphics graphics, Position begin, Position end, int width,
                                     int color1, int color2) {
        graphics.line(begin.x, begin.y, end.x, end.y, color1);
    }

    public static void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY, List<Component> tooltipTexts,
                                   ItemStack tooltipStack, @Nullable TooltipComponent tooltipComponent,
                                   Font tooltipFont) {
        graphics.renderTooltip(tooltipFont, tooltipTexts, Optional.ofNullable(tooltipComponent), mouseX, mouseY);
    }

    public static ClientTooltipComponent getClientTooltipComponent(TooltipComponent component) {
        return ClientTooltipComponent.create(component);
    }
}
