package mcjty.lib.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import mcjty.lib.base.StyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

public class RenderHelper {

    public static float rot = 0.0f;

    public static final int MAX_BRIGHTNESS = 0xf000f0;

    public static final RenderSettings DEFAULT_SETTINGS = RenderSettings.builder()
            .color(255, 255, 255)
            .alpha(128)
            .build();

    public static final RenderSettings FULLBRIGHT_SETTINGS = RenderSettings.builder()
            .color(255, 255, 255)
            .alpha(MAX_BRIGHTNESS)
            .build();

    /** The 26.1 replacement for {@code getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture)}. */
    public static TextureAtlasSprite blockSprite(Identifier texture) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
    }

    public static TextureAtlasSprite fluidSprite(FluidStack fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState()).stillMaterial().sprite();
    }

    public static int fluidTint(FluidStack fluid) {
        var tint = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState()).fluidTintSource();
        return tint == null ? -1 : tint.colorAsStack(fluid);
    }

    // Items in the world go through the submit pipeline, which only a RecordingBufferSource reaches.
    private static void submitItem(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, ItemDisplayContext context, int brightness, int combinedOverlay) {
        if (buffer instanceof RecordingBufferSource recording) {
            ItemStackRenderState state = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, context, Minecraft.getInstance().level, null, 0);
            state.submit(matrixStack, recording.collector(), brightness, combinedOverlay, 0);
        }
    }

    public static void renderItemGui(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int brightness, int combinedOverlay) {
        submitItem(matrixStack, buffer, stack, ItemDisplayContext.GUI, brightness, combinedOverlay);
    }

    public static void renderAndDecorateItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        graphics.item(stack, x, y, x * y * 31);
        graphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
    }

    public static void renderStaticFixed(PoseStack matrixStack, MultiBufferSource buffer, ItemStack stack, int brightness, int combinedOverlay) {
        submitItem(matrixStack, buffer, stack, ItemDisplayContext.FIXED, MAX_BRIGHTNESS, combinedOverlay);
    }

    public static void renderText(Font font, String text, int x, int y, int color, PoseStack poseStack, MultiBufferSource buffer, int lightmapValue) {
        font.drawInBatch(text, x, y, color, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, lightmapValue);
    }

    // Adjust the given matrix to the specified direction
    public static void adjustTransformToDirection(PoseStack matrixStack, Direction facing) {
        matrixStack.translate(0.5F, 0.5F, 0.5F);

        switch (facing) {
            case DOWN -> {
                matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
            case UP -> {
                matrixStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            case NORTH -> {
                matrixStack.mulPose(Axis.YP.rotationDegrees(-180.0F));
            }
            case SOUTH -> {
            }
            case WEST -> {
                matrixStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            case EAST -> {
                matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }

        matrixStack.translate(-0.5F, -0.5F, -0.5F);
    }

    /** Quarter turns of a face texture (NeoForge's datagen enum of the same name is gone in 26.1). */
    public enum FaceRotation {
        ZERO, CLOCKWISE_90, UPSIDE_DOWN, COUNTERCLOCKWISE_90
    }

    public static void renderNorthSouthQuad(PoseStack poseStack, VertexConsumer builder, TextureAtlasSprite sprite, FaceRotation rotation, float offset) {
        Matrix4f matrix = poseStack.last().pose();
        switch (rotation) {
            case ZERO -> {
                RenderHelper.vt(builder, matrix, 0, 1, .73f, sprite.getU0(), sprite.getV1());
                RenderHelper.vt(builder, matrix, 1, 1, .73f, sprite.getU1(), sprite.getV1());
                RenderHelper.vt(builder, matrix, 1, 0, .73f, sprite.getU1(), sprite.getV0());
                RenderHelper.vt(builder, matrix, 0, 0, .73f, sprite.getU0(), sprite.getV0());
            }
            case CLOCKWISE_90 -> {
                RenderHelper.vt(builder, matrix, 0, 1, .73f, sprite.getU1(), sprite.getV0());
                RenderHelper.vt(builder, matrix, 1, 1, .73f, sprite.getU0(), sprite.getV0());
                RenderHelper.vt(builder, matrix, 1, 0, .73f, sprite.getU0(), sprite.getV1());
                RenderHelper.vt(builder, matrix, 0, 0, .73f, sprite.getU1(), sprite.getV1());
            }
            case UPSIDE_DOWN -> {
                RenderHelper.vt(builder, matrix, 0, 1, .73f, sprite.getU0(), sprite.getV0());
                RenderHelper.vt(builder, matrix, 1, 1, .73f, sprite.getU0(), sprite.getV1());
                RenderHelper.vt(builder, matrix, 1, 0, .73f, sprite.getU1(), sprite.getV1());
                RenderHelper.vt(builder, matrix, 0, 0, .73f, sprite.getU1(), sprite.getV0());
            }
            case COUNTERCLOCKWISE_90 -> {
                RenderHelper.vt(builder, matrix, 0, 1, .73f, sprite.getU1(), sprite.getV1());
                RenderHelper.vt(builder, matrix, 1, 1, .73f, sprite.getU1(), sprite.getV0());
                RenderHelper.vt(builder, matrix, 1, 0, .73f, sprite.getU0(), sprite.getV0());
                RenderHelper.vt(builder, matrix, 0, 0, .73f, sprite.getU0(), sprite.getV1());
            }
        }
    }

    // Entities were never drawn here (the old entity render calls were already disabled upstream).
    public static boolean renderObject(GuiGraphicsExtractor graphics, int x, int y, Object itm, boolean highlight) {
        if (itm instanceof Entity) {
            return true;
        }
        if (itm == null) {
            return renderItemStack(graphics, ItemStack.EMPTY, x, y, "", highlight);
        }
        if (itm instanceof Item item) {
            return renderItemStack(graphics, new ItemStack(item, 1), x, y, "", highlight);
        }
        if (itm instanceof Block block) {
            return renderItemStack(graphics, new ItemStack(block, 1), x, y, "", highlight);
        }
        if (itm instanceof ItemStack stack) {
            return renderItemStackWithCount(graphics, stack, x, y, highlight);
        }
        if (itm instanceof FluidStack fluid) {
            return renderFluidStack(graphics, fluid, x, y, highlight);
        }
        if (itm instanceof TextureAtlasSprite) {
            return true;
        }
        return renderItemStack(graphics, ItemStack.EMPTY, x, y, "", highlight);
    }

    public static boolean renderFluidStack(GuiGraphicsExtractor graphics, FluidStack fluidStack, int x, int y, boolean highlight) {
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return false;
        }

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, fluidSprite(fluidStack), x, y, 16, 16, ARGB.opaque(fluidTint(fluidStack)));
        return true;
    }

    public static boolean renderItemStackWithCount(GuiGraphicsExtractor graphics, ItemStack itm, int xo, int yo, boolean highlight) {
        int size = itm.getCount();
        String amount;
        if (size <= 1) {
            amount = "";
        } else if (size < 100000) {
            amount = String.valueOf(size);
        } else if (size < 1000000) {
            amount = size / 1000 + "k";
        } else if (size < 1000000000) {
            amount = size / 1000000 + "m";
        } else {
            amount = size / 1000000000 + "g";
        }

        return renderItemStack(graphics, itm, xo, yo, amount, highlight);
    }

    public static boolean renderItemStack(GuiGraphicsExtractor graphics, ItemStack itm, int x, int y, String txt, boolean highlight) {
        if (highlight) {
            graphics.fillGradient(x, y, x + 16, y + 16, 0x80ffffff, 0xffffffff);
        }
        if (itm.isEmpty()) {
            return false;
        }
        graphics.item(itm, x, y, x * y * 31);
        renderGuiItemDecorations(graphics, Minecraft.getInstance().font, itm, x, y, txt, txt.length() - 2);
        return true;
    }

    private static void renderGuiItemDecorations(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, @Nullable String text, int scaled) {
        if (stack.getCount() != 1 || text != null) {
            String s = text == null ? String.valueOf(stack.getCount()) : text;
            if (scaled >= 2) {
                graphics.pose().pushMatrix();
                graphics.pose().scale(.5f, .5f);
                graphics.text(font, s, (x + 19 - 2) * 2 - 1 - font.width(s), y * 2 + 24, 0xffffffff, true);
                graphics.pose().popMatrix();
            } else if (scaled == 1) {
                graphics.pose().pushMatrix();
                graphics.pose().scale(.75f, .75f);
                graphics.text(font, s, (int) ((x - 2) * 1.34f + 24 - font.width(s)), (int) (y * 1.34f + 14), 0xffffffff, true);
                graphics.pose().popMatrix();
            } else {
                graphics.text(font, s, x + 19 - 2 - font.width(s), y + 6 + 3, 0xffffffff, true);
            }
        }

        if (stack.isBarVisible()) {
            graphics.fill(x + 2, y + 13, x + 15, y + 15, 0xff000000);
            graphics.fill(x + 2, y + 13, x + 2 + stack.getBarWidth(), y + 14, ARGB.opaque(stack.getBarColor()));
        }

        LocalPlayer localplayer = Minecraft.getInstance().player;
        float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        if (f > 0.0F) {
            graphics.fill(x, y + Mth.floor(16.0F * (1.0F - f)), x + 16, y + Mth.floor(16.0F * (1.0F - f)) + Mth.ceil(16.0F * f), 0x7fffffff);
        }
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     * x2 and y2 are not included.
     */
    public static void drawVerticalGradientRect(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color1, int color2) {
        graphics.fillGradient(x1, y1, x2, y2, color1, color2);
    }

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors.
     * x2 and y2 are not included.
     */
    public static void drawHorizontalGradientRect(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color1, int color2) {
        // 26.1's GUI has only vertical gradients, so a horizontal one is drawn a column at a time
        int width = x2 - x1;
        for (int i = 0; i < width; i++) {
            graphics.fill(x1 + i, y1, x1 + i + 1, y2, ARGB.srgbLerp(width <= 1 ? 0 : i / (float) (width - 1), color1, color2));
        }
    }

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors.
     * x2 and y2 are not included.
     */
    public static void drawHorizontalGradientRect(PoseStack poseStack, MultiBufferSource buffer, int x1, int y1, int x2, int y2, int color1, int color2, int lightmap) {
        float zLevel = -0.5f;

        float f = (color1 >> 24 & 255) / 255.0F;
        float f1 = (color1 >> 16 & 255) / 255.0F;
        float f2 = (color1 >> 8 & 255) / 255.0F;
        float f3 = (color1 & 255) / 255.0F;
        float f4 = (color2 >> 24 & 255) / 255.0F;
        float f5 = (color2 >> 16 & 255) / 255.0F;
        float f6 = (color2 >> 8 & 255) / 255.0F;
        float f7 = (color2 & 255) / 255.0F;

        VertexConsumer builder = buffer.getBuffer(CustomRenderTypes.QUADS_NOTEXTURE);

        Matrix4f positionMatrix = poseStack.last().pose();
        builder.addVertex(positionMatrix, x1, y1, zLevel).setColor(f1, f2, f3, f).setLight(lightmap);
        builder.addVertex(positionMatrix, x1, y2, zLevel).setColor(f1, f2, f3, f).setLight(lightmap);
        builder.addVertex(positionMatrix, x2, y2, zLevel).setColor(f5, f6, f7, f4).setLight(lightmap);
        builder.addVertex(positionMatrix, x2, y1, zLevel).setColor(f5, f6, f7, f4).setLight(lightmap);
    }

    public static void drawHorizontalLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int color) {
        graphics.fill(x1, y1, x2, y1 + 1, color);
    }

    public static void drawVerticalLine(GuiGraphicsExtractor graphics, int x1, int y1, int y2, int color) {
        graphics.fill(x1, y1, x1 + 1, y2, color);
    }

    // Draw a small triangle. x,y is the coordinate of the left point
    public static void drawLeftTriangle(GuiGraphicsExtractor graphics, int x, int y, int color) {
        drawVerticalLine(graphics, x, y, y, color);
        drawVerticalLine(graphics, x + 1, y - 1, y + 1, color);
        drawVerticalLine(graphics, x + 2, y - 2, y + 2, color);
    }

    // Draw a small triangle. x,y is the coordinate of the right point
    public static void drawRightTriangle(GuiGraphicsExtractor graphics, int x, int y, int color) {
        drawVerticalLine(graphics, x, y, y, color);
        drawVerticalLine(graphics, x - 1, y - 1, y + 1, color);
        drawVerticalLine(graphics, x - 2, y - 2, y + 2, color);
    }

    /** The text-selection highlight; 26.1 has no color logic op, its GUI_INVERT pipeline does the same job. */
    public static void drawColorLogic(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int red, int green, int blue) {
        graphics.fill(RenderPipelines.GUI_INVERT, x, y, x + width, y + height, ARGB.color(255, red, green, blue));
    }

    /**
     * Draw a button box. x2 and y2 are not included.
     */
    public static void drawThickButtonBox(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int bright, int average, int dark) {
        graphics.fill(x1 + 2, y1 + 2, x2 - 2, y2 - 2, average);
        drawHorizontalLine(graphics, x1 + 1, y1, x2 - 1, StyleConfig.colorButtonExternalBorder);
        drawHorizontalLine(graphics, x1 + 1, y2 - 1, x2 - 1, StyleConfig.colorButtonExternalBorder);
        drawVerticalLine(graphics, x1, y1 + 1, y2 - 1, StyleConfig.colorButtonExternalBorder);
        drawVerticalLine(graphics, x2 - 1, y1 + 1, y2 - 1, StyleConfig.colorButtonExternalBorder);

        drawHorizontalLine(graphics, x1 + 1, y1 + 1, x2 - 1, bright);
        drawHorizontalLine(graphics, x1 + 2, y1 + 2, x2 - 2, bright);
        drawVerticalLine(graphics, x1 + 1, y1 + 2, y2 - 2, bright);
        drawVerticalLine(graphics, x1 + 2, y1 + 3, y2 - 3, bright);

        drawHorizontalLine(graphics, x1 + 3, y2 - 3, x2 - 2, dark);
        drawHorizontalLine(graphics, x1 + 2, y2 - 2, x2 - 1, dark);
        drawVerticalLine(graphics, x2 - 2, y1 + 2, y2 - 2, dark);
        drawVerticalLine(graphics, x2 - 3, y1 + 3, y2 - 3, dark);
    }

    /**
     * Draw a button box. x2 and y2 are not included.
     */
    public static void drawThinButtonBox(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int bright, int average, int dark) {
        graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, average);
        drawHorizontalLine(graphics, x1 + 1, y1, x2 - 1, StyleConfig.colorButtonExternalBorder);
        drawHorizontalLine(graphics, x1 + 1, y2 - 1, x2 - 1, StyleConfig.colorButtonExternalBorder);
        drawVerticalLine(graphics, x1, y1 + 1, y2 - 1, StyleConfig.colorButtonExternalBorder);
        drawVerticalLine(graphics, x2 - 1, y1 + 1, y2 - 1, StyleConfig.colorButtonExternalBorder);

        drawHorizontalLine(graphics, x1 + 1, y1 + 1, x2 - 2, bright);
        drawVerticalLine(graphics, x1 + 1, y1 + 2, y2 - 3, bright);

        drawHorizontalLine(graphics, x1 + 1, y2 - 2, x2 - 1, dark);
        drawVerticalLine(graphics, x2 - 2, y1 + 1, y2 - 2, dark);
    }

    /**
     * Draw a button box. x2 and y2 are not included.
     */
    public static void drawThinButtonBoxGradient(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int bright, int average1, int average2, int dark) {
        drawVerticalGradientRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, average2, average1);
        drawHorizontalLine(graphics, x1 + 1, y1, x2 - 1, StyleConfig.colorButtonExternalBorder);
        drawHorizontalLine(graphics, x1 + 1, y2 - 1, x2 - 1, StyleConfig.colorButtonExternalBorder);
        drawVerticalLine(graphics, x1, y1 + 1, y2 - 1, StyleConfig.colorButtonExternalBorder);
        drawVerticalLine(graphics, x2 - 1, y1 + 1, y2 - 1, StyleConfig.colorButtonExternalBorder);

        drawHorizontalLine(graphics, x1 + 1, y1 + 1, x2 - 2, bright);
        drawVerticalLine(graphics, x1 + 1, y1 + 2, y2 - 3, bright);

        drawHorizontalLine(graphics, x1 + 1, y2 - 2, x2 - 1, dark);
        drawVerticalLine(graphics, x2 - 2, y1 + 1, y2 - 2, dark);
    }

    /**
     * Draw a button box. x2 and y2 are not included.
     */
    public static void drawFlatButtonBox(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int bright, int average, int dark) {
        drawBeveledBox(graphics, x1, y1, x2, y2, bright, dark, average);
    }

    /**
     * Draw a button box. x2 and y2 are not included.
     */
    public static void drawFlatButtonBox(PoseStack poseStack, MultiBufferSource buffer, int x1, int y1, int x2, int y2, int bright, int average, int dark, int lightmap) {
        drawBeveledBox(poseStack, buffer, x1, y1, x2, y2, bright, dark, average, lightmap);
    }

    /**
     * Draw a button box. x2 and y2 are not included.
     */
    public static void drawFlatButtonBoxGradient(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int bright, int average1, int average2, int dark) {
        drawVerticalGradientRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, average2, average1);
        drawHorizontalLine(graphics, x1, y1, x2 - 1, bright);
        drawVerticalLine(graphics, x1, y1, y2 - 1, bright);
        drawVerticalLine(graphics, x2 - 1, y1, y2 - 1, dark);
        drawHorizontalLine(graphics, x1, y2 - 1, x2, dark);
    }

    /**
     * Draw a beveled box. x2 and y2 are not included.
     */
    public static void drawBeveledBox(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int topleftcolor, int botrightcolor, int fillcolor) {
        if (fillcolor != -1) {
            graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillcolor);
        }
        drawHorizontalLine(graphics, x1, y1, x2 - 1, topleftcolor);
        drawVerticalLine(graphics, x1, y1, y2 - 1, topleftcolor);
        drawVerticalLine(graphics, x2 - 1, y1, y2 - 1, botrightcolor);
        drawHorizontalLine(graphics, x1, y2 - 1, x2, botrightcolor);
    }

    /**
     * Draw a beveled box. x2 and y2 are not included. Use this version for in-world screens
     */
    public static void drawBeveledBox(PoseStack poseStack, MultiBufferSource buffer, int x1, int y1, int x2, int y2, int topleftcolor, int botrightcolor, int fillcolor, int lightmap) {
        if (fillcolor != -1) {
            fill(poseStack, buffer, x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillcolor, lightmap);
        }
        fill(poseStack, buffer, x1, y1, x2 - 1, y1 + 1, topleftcolor, lightmap);
        fill(poseStack, buffer, x1, y1, x1 + 1, y2 - 1, topleftcolor, lightmap);
        fill(poseStack, buffer, x2 - 1, y1, x2 - 1 + 1, y2 - 1, botrightcolor, lightmap);
        fill(poseStack, buffer, x1, y2 - 1, x2, y2 - 1 + 1, botrightcolor, lightmap);
    }

    /**
     * Draw a thick beveled box. x2 and y2 are not included. Use this version for batched rendering
     */
    public static void drawThickBeveledBox(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int thickness, int topleftcolor, int botrightcolor, int fillcolor) {
        if (fillcolor != -1) {
            graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillcolor);
        }
        graphics.fill(x1, y1, x2 - 1, y1 + thickness, topleftcolor);
        graphics.fill(x1, y1, x1 + thickness, y2 - 1, topleftcolor);
        graphics.fill(x2 - thickness, y1, x2, y2 - 1, botrightcolor);
        graphics.fill(x1, y2 - thickness, x2, y2, botrightcolor);
    }

    /**
     * Draw a beveled box. x2 and y2 are not included.
     */
    public static void drawFlatBox(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int border, int fill) {
        if (fill != -1) {
            graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fill);
        }
        drawHorizontalLine(graphics, x1, y1, x2 - 1, border);
        drawVerticalLine(graphics, x1, y1, y2 - 1, border);
        drawVerticalLine(graphics, x2 - 1, y1, y2 - 1, border);
        drawHorizontalLine(graphics, x1, y2 - 1, x2, border);
    }

    public static void drawTexturedModalRect(PoseStack poseStack, VertexConsumer builder, int x, int y, int textureX, int textureY, int width, int height, int totw, int toth, float parentU, float parentV) {
        Matrix4f matrix = poseStack.last().pose();
        float f = 1.0f / totw;
        float f1 = 1.0f / toth;
        float zLevel = 50;
        builder.addVertex(matrix, (x + 0), (y + height), zLevel).setUv(parentU + ((textureX + 0) * f), parentV + ((textureY + height) * f1));
        builder.addVertex(matrix, (x + width), (y + height), zLevel).setUv(parentU + ((textureX + width) * f), parentV + ((textureY + height) * f1));
        builder.addVertex(matrix, (x + width), (y + 0), zLevel).setUv(parentU + ((textureX + width) * f), parentV + ((textureY + 0) * f1));
        builder.addVertex(matrix, (x + 0), (y + 0), zLevel).setUv(parentU + ((textureX + 0) * f), parentV + ((textureY + 0) * f1));
    }

    /**
     * Draws part of a 256x256 texture. Args: texture, x, y, u, v, width, height
     */
    public static void drawTexturedModalRect(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256);
    }

    public static void renderBillboardQuadBright(PoseStack matrixStack, MultiBufferSource buffer, float scale, Identifier texture) {
        renderBillboardQuadBright(matrixStack, buffer, scale, texture, DEFAULT_SETTINGS);
    }

    public static void renderBillboardQuadBright(PoseStack matrixStack, VertexConsumer builder, float scale, Identifier texture, RenderSettings settings) {
        int b1 = settings.brightness() >> 16 & 65535;
        int b2 = settings.brightness() & 65535;
        TextureAtlasSprite sprite = blockSprite(texture);
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);
        RenderHelper.rotateToPlayer(matrixStack);
        Matrix4f matrix = matrixStack.last().pose();
        builder.addVertex(matrix, -scale, -scale, 0.0f).setColor(settings.r(), settings.g(), settings.b(), settings.a()).setUv(sprite.getU0(), sprite.getV0()).setUv2(b1, b2).setNormal(1, 0, 0);
        builder.addVertex(matrix, -scale, scale, 0.0f).setColor(settings.r(), settings.g(), settings.b(), settings.a()).setUv(sprite.getU0(), sprite.getV1()).setUv2(b1, b2).setNormal(1, 0, 0);
        builder.addVertex(matrix, scale, scale, 0.0f).setColor(settings.r(), settings.g(), settings.b(), settings.a()).setUv(sprite.getU1(), sprite.getV1()).setUv2(b1, b2).setNormal(1, 0, 0);
        builder.addVertex(matrix, scale, -scale, 0.0f).setColor(settings.r(), settings.g(), settings.b(), settings.a()).setUv(sprite.getU1(), sprite.getV0()).setUv2(b1, b2).setNormal(1, 0, 0);
        matrixStack.popPose();
    }

    public static void renderBillboardQuadBright(PoseStack poseStack, MultiBufferSource buffer, float scale, Identifier texture, RenderSettings settings) {
        renderBillboardQuadBright(poseStack, buffer.getBuffer(settings.renderType()), scale, texture, settings);
    }

    public static int renderText(GuiGraphicsExtractor graphics, int x, int y, String txt) {
        Font font = Minecraft.getInstance().font;
        graphics.text(font, txt, x, y, 0xffffffff, true);
        return font.width(txt);
    }

    public static int renderText(GuiGraphicsExtractor graphics, int x, int y, String txt, int color) {
        Font font = Minecraft.getInstance().font;
        graphics.text(font, txt, x, y, ARGB.opaque(color), false);
        return font.width(txt);
    }

    public static void rotateToPlayer(PoseStack poseStack) {
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.PI));  // Flip to face the camera
    }

    /**
     * Draw a beam with some thickness.
     */
    public static void drawBeam(PoseStack matrix, VertexConsumer builder, TextureAtlasSprite sprite, Vec3 S, Vec3 E, Vec3 P, float width) {
        Vec3 PS = S.subtract(P);
        Vec3 SE = E.subtract(S);

        Vec3 normal = PS.cross(SE).normalize();

        Vec3 half = normal.multiply(width, width, width);
        Vec3 p1 = S.add(half);
        Vec3 p2 = S.subtract(half);
        Vec3 p3 = E.add(half);
        Vec3 p4 = E.subtract(half);

        drawQuad(matrix.last().pose(), builder, sprite, p1, p3, p4, p2, DEFAULT_SETTINGS);
    }

    /**
     * Draw a beam with some thickness.
     */
    public static void drawBeam(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, Vec3 S, Vec3 E, Vec3 P, RenderSettings settings) {
        Vec3 PS = S.subtract(P);
        Vec3 SE = E.subtract(S);

        Vec3 normal = PS.cross(SE).normalize();

        Vec3 half = normal.multiply(settings.width(), settings.width(), settings.width());
        Vec3 p1 = S.add(half);
        Vec3 p2 = S.subtract(half);
        Vec3 p3 = E.add(half);
        Vec3 p4 = E.subtract(half);

        drawQuad(poseStack.last().pose(), buffer, sprite, p1, p3, p4, p2, settings);
    }

    public static void drawQuadGui(PoseStack poseStack, VertexConsumer builder,
                                   float x1, float x2, float y1, float y2, float z,
                                   int color, int packedLightIn) {
        Matrix4f matrix = poseStack.last().pose();
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        builder.addVertex(matrix, x1, y2, z).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y2, z).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y1, z).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y1, z).setColor(r, g, b, a).setLight(packedLightIn);
    }

    private static void drawQuad(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4,
                                 RenderSettings settings) {
        int b1 = settings.brightness() >> 16 & 65535;
        int b2 = settings.brightness() & 65535;

        vt(buffer, matrix, (float) p1.x(), (float) p1.y(), (float) p1.z(), sprite.getU0(), sprite.getV0(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
        vt(buffer, matrix, (float) p2.x(), (float) p2.y(), (float) p2.z(), sprite.getU1(), sprite.getV0(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
        vt(buffer, matrix, (float) p3.x(), (float) p3.y(), (float) p3.z(), sprite.getU1(), sprite.getV1(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
        vt(buffer, matrix, (float) p4.x(), (float) p4.y(), (float) p4.z(), sprite.getU0(), sprite.getV1(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
    }

    private static void drawQuad(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4,
                                 boolean opposite,
                                 RenderSettings settings) {
        int b1 = settings.brightness() >> 16 & 65535;
        int b2 = settings.brightness() & 65535;

        if (opposite) {
            vt(buffer, matrix, (float) p1.x(), (float) p1.y(), (float) p1.z(), sprite.getU0(), sprite.getV0(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
            vt(buffer, matrix, (float) p2.x(), (float) p2.y(), (float) p2.z(), sprite.getU1(), sprite.getV0(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
            vt(buffer, matrix, (float) p3.x(), (float) p3.y(), (float) p3.z(), sprite.getU1(), sprite.getV1(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
            vt(buffer, matrix, (float) p4.x(), (float) p4.y(), (float) p4.z(), sprite.getU0(), sprite.getV1(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
        } else {
            vt(buffer, matrix, (float) p4.x(), (float) p4.y(), (float) p4.z(), sprite.getU0(), sprite.getV1(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
            vt(buffer, matrix, (float) p3.x(), (float) p3.y(), (float) p3.z(), sprite.getU1(), sprite.getV1(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
            vt(buffer, matrix, (float) p2.x(), (float) p2.y(), (float) p2.z(), sprite.getU1(), sprite.getV0(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
            vt(buffer, matrix, (float) p1.x(), (float) p1.y(), (float) p1.z(), sprite.getU0(), sprite.getV0(), b1, b2, settings.r(), settings.g(), settings.b(), settings.a());
        }
    }

    /**
     * Render a block outline
     */

    /**
     * Draw a box. No UV mapping
     */
    public static void drawBox(PoseStack matrixStack, VertexConsumer builder,
                               float x1, float x2, float y1, float y2, float z1, float z2,
                               float r, float g, float b,
                               int packedLightIn) {
        drawBox(matrixStack, builder, x1, x2, y1, y2, z1, z2, r, g, b, 1.0f, packedLightIn);
    }

    /**
     * Draw a box with alpha. No UV mapping
     */
    public static void drawBox(PoseStack matrixStack, VertexConsumer builder,
                               float x1, float x2, float y1, float y2, float z1, float z2,
                               float r, float g, float b, float a,
                               int packedLightIn) {
        Matrix4f matrix = matrixStack.last().pose();
        // BACK
        builder.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);

        // FRONT
        builder.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);

        // DOWN
        builder.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);

        // UP
        builder.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);

        // LEFT
        builder.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);

        // RIGHT
        builder.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setLight(packedLightIn);
        builder.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setLight(packedLightIn);
    }

    /**
     * Draw a box. The texture is mapped from 0,0 -> 1,1 on each face regardless of the size of that face
     */
    public static void drawBox(PoseStack matrixStack, VertexConsumer builder, TextureAtlasSprite sprite,
                               boolean down, boolean up, boolean north, boolean south, boolean west, boolean east,
                               float x1, float x2, float y1, float y2, float z1, float z2, RenderSettings settings) {
        Matrix4f matrix = matrixStack.last().pose();
        Vec3 c111 = new Vec3(x1, y1, z1);
        Vec3 c112 = new Vec3(x1, y1, z2);
        Vec3 c121 = new Vec3(x1, y2, z1);
        Vec3 c122 = new Vec3(x1, y2, z2);
        Vec3 c211 = new Vec3(x2, y1, z1);
        Vec3 c212 = new Vec3(x2, y1, z2);
        Vec3 c221 = new Vec3(x2, y2, z1);
        Vec3 c222 = new Vec3(x2, y2, z2);
        if (down) {
            drawQuad(matrix, builder, sprite, c211, c212, c112, c111, settings);
        }
        if (up) {
            drawQuad(matrix, builder, sprite, c121, c122, c222, c221, settings);
        }
        if (north) {
            drawQuad(matrix, builder, sprite, c121, c221, c211, c111, settings);
        }
        if (south) {
            drawQuad(matrix, builder, sprite, c112, c212, c222, c122, settings);
        }
        if (west) {
            drawQuad(matrix, builder, sprite, c112, c122, c121, c111, settings);
        }
        if (east) {
            drawQuad(matrix, builder, sprite, c211, c221, c222, c212, settings);
        }
    }

    /**
     * Draw a box. The texture is mapped from 0,0 -> 1,1 on each face regardless of the size of that face
     */
    public static void drawBox(PoseStack matrixStack, VertexConsumer builder, TextureAtlasSprite sprite,
                               float x1, float x2, float y1, float y2, float z1, float z2, RenderSettings settings) {
        Matrix4f matrix = matrixStack.last().pose();
        Vec3 c111 = new Vec3(x1, y1, z1);
        Vec3 c112 = new Vec3(x1, y1, z2);
        Vec3 c121 = new Vec3(x1, y2, z1);
        Vec3 c122 = new Vec3(x1, y2, z2);
        Vec3 c211 = new Vec3(x2, y1, z1);
        Vec3 c212 = new Vec3(x2, y1, z2);
        Vec3 c221 = new Vec3(x2, y2, z1);
        Vec3 c222 = new Vec3(x2, y2, z2);
        drawQuad(matrix, builder, sprite, c211, c212, c112, c111, settings);
        drawQuad(matrix, builder, sprite, c121, c122, c222, c221, settings);
        drawQuad(matrix, builder, sprite, c121, c221, c211, c111, settings);
        drawQuad(matrix, builder, sprite, c112, c212, c222, c122, settings);
        drawQuad(matrix, builder, sprite, c112, c122, c121, c111, settings);
        drawQuad(matrix, builder, sprite, c211, c221, c222, c212, settings);
    }

    /**
     * Draw a box. The texture is mapped from 0,0 -> 1,1 on each face regardless of the size of that face
     */

    /**
     * Draw a box. The texture is mapped according to unit size and will scale with the size of every face
     */

    public static void drawQuad(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, Direction side, boolean opposite, float offset, RenderSettings settings) {
        Matrix4f matrix = poseStack.last().pose();
        switch (side) {
            case DOWN -> drawQuad(matrix, buffer, sprite,
                    new Vec3(0, offset, 1),
                    new Vec3(1, offset, 1),
                    new Vec3(1, offset, 0),
                    new Vec3(0, offset, 0), opposite, settings);
            case UP -> drawQuad(matrix, buffer, sprite,
                    new Vec3(1, 1 - offset, 1),
                    new Vec3(0, 1 - offset, 1),
                    new Vec3(0, 1 - offset, 0),
                    new Vec3(1, 1 - offset, 0), opposite, settings);
            case NORTH -> drawQuad(matrix, buffer, sprite,
                    new Vec3(0, 0, offset),
                    new Vec3(1, 0, offset),
                    new Vec3(1, 1, offset),
                    new Vec3(0, 1, offset), opposite, settings);
            case SOUTH -> drawQuad(matrix, buffer, sprite,
                    new Vec3(0, 1, 1 - offset),
                    new Vec3(1, 1, 1 - offset),
                    new Vec3(1, 0, 1 - offset),
                    new Vec3(0, 0, 1 - offset), opposite, settings);
            case WEST -> drawQuad(matrix, buffer, sprite,
                    new Vec3(offset, 0, 0),
                    new Vec3(offset, 1, 0),
                    new Vec3(offset, 1, 1),
                    new Vec3(offset, 0, 1), opposite, settings);
            case EAST -> drawQuad(matrix, buffer, sprite,
                    new Vec3(1 - offset, 0, 1),
                    new Vec3(1 - offset, 1, 1),
                    new Vec3(1 - offset, 1, 0),
                    new Vec3(1 - offset, 0, 0), opposite, settings);
        }
    }

    public static void vt(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float r, float g, float b,
                          int packedLight) {
        renderer.addVertex(stack.last().pose(), x, y, z).setColor(r, g, b, 1f).setLight(packedLight).setNormal(1.0F, 0.0F, 0.0F);
    }

    public static void vt(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float u, float v,
                          int packedLight) {
        renderer.addVertex(stack.last().pose(), x, y, z).setColor(1f, 1f, 1f, 1f).setUv(u, v).setLight(packedLight).setNormal(1.0F, 0.0F, 0.0F);
    }

    private static void vt(VertexConsumer renderer, Matrix4f matrix, float x, float y, float z, float u, float v, int packedLight) {
        renderer.addVertex(matrix, x, y, z).setColor(1f, 1f, 1f, 1f).setUv(u, v).setLight(packedLight).setNormal(1.0F, 0.0F, 0.0F);
    }

    public static void vt(VertexConsumer renderer, PoseStack matrix, float x, float y, float z, float u, float v, int lu, int lv, int r, int g, int b, int a) {
        renderer
                .addVertex(matrix.last().pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setUv2(lu, lv)
                .setNormal(1, 0, 0)
                ;
    }


    private static void vt(VertexConsumer renderer, Matrix4f matrix, float x, float y, float z, float u, float v) {
        renderer
                .addVertex(matrix, x, y, z)
                .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                .setUv(u, v)
                .setLight(MAX_BRIGHTNESS)
                .setNormal(1, 0, 0)
                ;
    }

    private static void vt(VertexConsumer renderer, Matrix4f matrix, float x, float y, float z, float u, float v, int lu, int lv, int r, int g, int b, int a) {
        renderer
                .addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setUv2(lu, lv)
                .setNormal(1, 0, 0)
                ;
    }

    public static void putVertex(VertexConsumer builder, Position normal,
                                 double x, double y, double z, float u, float v,
                                 TextureAtlasSprite sprite, float r, float g, float b, float a) {
        float iu = sprite.getU(u);
        float iv = sprite.getV(v);
        builder.addVertex((float) x, (float) y, (float) z)
                .setUv(iu, iv)
                .setUv2(0, 0)
                .setColor(r, g, b, a)
                .setNormal((float) normal.x(), (float) normal.y(), (float) normal.z())
                ;
    }

    public static void renderHighLightedBlocksOutline(PoseStack poseStack, VertexConsumer buffer, float mx, float my, float mz, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, mx, my, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my + 1, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my + 1, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my + 1, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my + 1, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my + 1, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my + 1, mz).setColor(r, g, b, a);

        buffer.addVertex(matrix, mx, my + 1, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my + 1, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my + 1, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my + 1, mz).setColor(r, g, b, a);

        buffer.addVertex(matrix, mx + 1, my, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my, mz).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my + 1, mz).setColor(r, g, b, a);

        buffer.addVertex(matrix, mx, my, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx + 1, my, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my, mz + 1).setColor(r, g, b, a);
        buffer.addVertex(matrix, mx, my + 1, mz + 1).setColor(r, g, b, a);
    }

    public static void fill(PoseStack poseStack, MultiBufferSource buffer, int x1, int y1, int x2, int y2, int color,
                            int lightmap) {
        Matrix4f positionMatrix = poseStack.last().pose();
        int swapper;
        if (x1 < x2) {
            swapper = x1;
            x1 = x2;
            x2 = swapper;
        }

        if (y1 < y2) {
            swapper = y1;
            y1 = y2;
            y2 = swapper;
        }

        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        VertexConsumer builder = buffer.getBuffer(CustomRenderTypes.QUADS_NOTEXTURE);
//        RenderSystem.enableBlend();
//        RenderSystem.disableTexture();
//        RenderSystem.defaultBlendFunc();
//        builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        builder.addVertex(positionMatrix, x1, y2, -1F).setColor(r, g, b, a).setLight(lightmap);
        builder.addVertex(positionMatrix, x2, y2, -1F).setColor(r, g, b, a).setLight(lightmap);
        builder.addVertex(positionMatrix, x2, y1, -1F).setColor(r, g, b, a).setLight(lightmap);
        builder.addVertex(positionMatrix, x1, y1, -1F).setColor(r, g, b, a).setLight(lightmap);
//        builder.finishDrawing();
//        RenderSystem.enableTexture();
//        RenderSystem.disableBlend();
    }

    public static void rotateXP(PoseStack stack, float degrees) {
        stack.mulPose(Axis.XP.rotationDegrees(degrees));
    }

    public static void rotateYP(PoseStack stack, float degrees) {
        stack.mulPose(Axis.YP.rotationDegrees(degrees));
    }

    public record Rect(Vec3 v1, Vec3 v2,
                       Vec3 v3, Vec3 v4) {
    }
}
