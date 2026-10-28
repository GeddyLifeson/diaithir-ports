package com.lowdragmc.lowdraglib.gui.util;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;

import com.lowdragmc.lowdraglib.client.shader.management.ShaderProgram;
import com.lowdragmc.lowdraglib.client.shader.uniform.UniformCache;
import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Rect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class DrawerHelper {

    public static ShaderProgram ROUND;
    public static ShaderProgram PANEL_BG;
    public static ShaderProgram ROUND_BOX;
    public static ShaderProgram PROGRESS_ROUND_BOX;
    public static ShaderProgram FRAME_ROUND_BOX;
    public static ShaderProgram ROUND_LINE;

    public static void init() {
        ROUND = Util.make(new ShaderProgram(), program
                -> program.attach(Shaders.ROUND_F).attach(Shaders.SCREEN_V));
        PANEL_BG = Util.make(new ShaderProgram(), program
                -> program.attach(Shaders.PANEL_BG_F).attach(Shaders.SCREEN_V));
        ROUND_BOX = Util.make(new ShaderProgram(), program
                -> program.attach(Shaders.ROUND_BOX_F).attach(Shaders.SCREEN_V));
        PROGRESS_ROUND_BOX = Util.make(new ShaderProgram(), program
                -> program.attach(Shaders.PROGRESS_ROUND_BOX_F).attach(Shaders.SCREEN_V));
        FRAME_ROUND_BOX = Util.make(new ShaderProgram(), program
                -> program.attach(Shaders.FRAME_ROUND_BOX_F).attach(Shaders.SCREEN_V));
        ROUND_LINE = Util.make(new ShaderProgram(), program
                -> program.attach(Shaders.ROUND_LINE_F).attach(Shaders.SCREEN_V));
    }

    public static void drawFluidTexture(@Nonnull GuiGraphics graphics, float xCoord, float yCoord, TextureAtlasSprite textureSprite, float maskTop, float maskRight, float zLevel, int fluidColor) {  /* ported stub */ }

    @Deprecated(since = "1.21")
    public static void drawFluidForGui(@Nonnull GuiGraphics graphics, FluidStack contents, long tankCapacity, int startX, int startY, int widthT, int heightT) {
        drawFluidForGui(graphics, contents, startX, startY, widthT, heightT);
    }

    public static void drawFluidForGui(@Nonnull GuiGraphics graphics, FluidStack contents, float startX, float startY, float widthT, float heightT) {  /* ported stub */ }

    public static void drawBorder(@Nonnull GuiGraphics graphics, int x, int y, int width, int height, int color, int border) {  /* ported stub */ }

    public static void drawStringSized(@Nonnull GuiGraphics graphics, String text, float x, float y, int color, boolean dropShadow, float scale, boolean center) {
        graphics.pose().pushPose();
        Font fontRenderer = Minecraft.getInstance().font;
        double scaledTextWidth = center ? fontRenderer.width(text) * scale : 0.0;
        graphics.pose().translate(x - scaledTextWidth / 2.0, y, 0.0f);
        graphics.pose().scale(scale, scale, scale);
        graphics.drawString(fontRenderer, text, 0, 0, color, dropShadow);
        graphics.pose().popPose();
    }

    public static void drawStringFixedCorner(@Nonnull GuiGraphics graphics, String text, float x, float y, int color, boolean dropShadow, float scale) {
        Font fontRenderer = Minecraft.getInstance().font;
        float scaledWidth = fontRenderer.width(text) * scale;
        float scaledHeight = fontRenderer.lineHeight * scale;
        drawStringSized(graphics, text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    public static void drawText(@Nonnull GuiGraphics graphics, String text, float x, float y, float scale, int color) {
        drawText(graphics, text, x, y, scale, color, false);
    }

    public static void drawText(@Nonnull GuiGraphics graphics, String text, float x, float y, float scale, int color, boolean shadow) {  /* ported stub */ }

    public static void drawItemStack(@Nonnull GuiGraphics graphics, ItemStack itemStack, int x, int y, int color, @Nullable String altTxt) {  /* ported stub */ }

    public static List<Component> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getInstance();
        return Screen.getTooltipFromItem(mc, itemStack);
    }

    public static void drawSolidRect(@Nonnull GuiGraphics graphics, int x, int y, int width, int height, int color) {  /* ported stub */ }

    public static void drawSolidRect(@Nonnull GuiGraphics graphics, Rect rect, int color) {
        drawSolidRect(graphics, rect.left, rect.up, rect.right, rect.down, color);
    }

    public static void drawRectShadow(@Nonnull GuiGraphics graphics, int x, int y, int width, int height, int distance) {  /* ported stub */ }

    public static void drawGradientRect(@Nonnull GuiGraphics graphics, int x, int y, int width, int height, int startColor, int endColor) {
        drawGradientRect(graphics, x, y, width, height, startColor, endColor, false);
    }

    public static void drawGradientRect(@Nonnull GuiGraphics graphics, float x, float y, float width, float height, int startColor, int endColor, boolean horizontal) {  /* ported stub */ }

    public static void drawLines(@Nonnull GuiGraphics graphics, List<Vec2> Vec2s, int startColor, int endColor, float width) {  /* ported stub */ }

    public static void drawTextureRect(@Nonnull GuiGraphics graphics, float x, float y, float width, float height) {  /* ported stub */ }

    public static void updateScreenVshUniform(@Nonnull GuiGraphics graphics, UniformCache uniform) {  /* ported stub */ }

    public static void drawRound(@Nonnull GuiGraphics graphics, int color, float radius, Position centerPos) {  /* ported stub */ }

    public static void drawPanelBg(@Nonnull GuiGraphics graphics) {  /* ported stub */ }

    public static void drawRoundBox(@Nonnull GuiGraphics graphics, Rect square, Vector4f radius, int color) {  /* ported stub */ }

    public static void drawProgressRoundBox(@Nonnull GuiGraphics graphics, Rect square, Vector4f radius, int color1, int color2, float progress) {  /* ported stub */ }

    public static void drawFrameRoundBox(@Nonnull GuiGraphics graphics, Rect square, float thickness, Vector4f radius1, Vector4f radius2, int color) {  /* ported stub */ }

    public static void drawRoundLine(@Nonnull GuiGraphics graphics, Position begin, Position end, int width, int color1, int color2) {  /* ported stub */ }

    private static void uploadScreenPosVertex() {  /* ported stub */ }

    public static void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY, List<Component> tooltipTexts, ItemStack tooltipStack, @Nullable TooltipComponent tooltipComponent, Font tooltipFont) {  /* ported stub */ }

    public static ClientTooltipComponent getClientTooltipComponent(TooltipComponent component) {
        return ClientTooltipComponent.create(component);
    }
}
