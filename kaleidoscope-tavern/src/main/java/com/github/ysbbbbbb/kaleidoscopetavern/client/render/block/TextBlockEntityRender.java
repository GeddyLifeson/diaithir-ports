package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.block.deco.ChalkboardBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.TextBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.TextBlockRenderState;
import com.github.ysbbbbbb.kaleidoscopetavern.util.TextAlignment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 文本类方块实体渲染器抽象基类（NeoForge 26.1.2 新渲染 API）
 */
public abstract class TextBlockEntityRender<T extends TextBlockEntity, S extends TextBlockRenderState> implements BlockEntityRenderer<T, S> {
    protected static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    /**
     * 文本最大渲染距离（格），超过此距离不渲染文字
     */
    protected static final double TEXT_RENDER_DISTANCE_SQ = 48.0 * 48.0;

    protected final Font font;

    public TextBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public void extractRenderState(T textBlock, S state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(textBlock, state, partialTicks, cameraPosition, breakProgress);

        // 提取朝向
        state.facing = textBlock.getBlockState().getValue(ChalkboardBlock.FACING);

        // 计算相机到方块中心的距离平方，决定是否渲染文字
        BlockPos pos = textBlock.getBlockPos();
        double dx = cameraPosition.x - (pos.getX() + 0.5);
        double dy = cameraPosition.y - (pos.getY() + 0.5);
        double dz = cameraPosition.z - (pos.getZ() + 0.5);
        double distSq = dx * dx + dy * dy + dz * dz;

        // 文字内容：距离超过 48 格则置空以跳过渲染
        String rawText = textBlock.getText();
        state.text = (distSq <= TEXT_RENDER_DISTANCE_SQ && StringUtils.isNotBlank(rawText)) ? rawText : "";

        // 颜色、发光、对齐
        state.color = textBlock.getColor();
        state.isGlowing = textBlock.isGlowing();
        state.textAlignment = textBlock.getTextAlignment();
        state.isLarge = false;
    }

    /**
     * 渲染文字的辅助方法。
     * <p>
     * 调用前调用者应已完成坐标变换（translate + rotate），调用后负责 popPose()。
     *
     * @param state               当前渲染状态
     * @param poseStack           已就位的矩阵栈
     * @param submitNodeCollector 提交节点收集器
     * @param text                要渲染的文本（已确认非空）
     * @param maxWidth            文本最大宽度（像素）
     * @param scale               缩放比例
     * @param maxLines            最大行数
     * @param lineHeight          行高（像素）
     */
    protected void doTextRender(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                String text, int maxWidth, float scale, int maxLines, int lineHeight) {
        poseStack.scale(scale, -scale, scale);

        int darkColor = getDarkColor(state.color, state.isGlowing);
        final int textColor;
        final int outlineColor;
        final int light;

        if (state.isGlowing) {
            textColor = state.color.getTextColor();
            boolean drawOutline = isOutlineVisible(state);
            outlineColor = drawOutline ? darkColor : 0;
            light = 0xf000f0;
        } else {
            textColor = darkColor;
            outlineColor = 0;
            light = state.lightCoords;
        }

        Component renderText = getRenderText(text);
        List<FormattedCharSequence> splitLines = font.split(renderText, maxWidth);
        int totalLines = Math.min(splitLines.size(), maxLines);

        for (int i = 0; i < totalLines; i++) {
            FormattedCharSequence line = splitLines.get(i);
            float posX = getPosX(state, maxWidth, font.width(line));
            float posY = i * lineHeight - 19;

            submitNodeCollector.submitText(
                    poseStack, posX, posY, line, false,
                    Font.DisplayMode.POLYGON_OFFSET,
                    light, textColor, 0, outlineColor
            );
        }
    }

    protected Component getRenderText(String text) {
        return Component.literal(text);
    }

    protected float getPosX(S state, int maxWidth, int lineWidth) {
        TextAlignment alignment = state.textAlignment;
        if (alignment == TextAlignment.LEFT) {
            return -maxWidth / 2f;
        } else if (alignment == TextAlignment.RIGHT) {
            return maxWidth / 2f - lineWidth;
        } else {
            return -lineWidth / 2f;
        }
    }

    protected int getDarkColor(DyeColor color, boolean isGlowing) {
        int textColor = color.getTextColor();
        if (textColor == DyeColor.BLACK.getTextColor() && isGlowing) {
            return 0xff_f0ebcc;
        }
        return ARGB.scaleRGB(textColor, 0.6f);
    }

    protected boolean isOutlineVisible(S state) {
        int textColor = state.color.getTextColor();
        if (textColor == DyeColor.BLACK.getTextColor()) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && mc.options.getCameraType().isFirstPerson() && player.isScoping()) {
            return true;
        }
        Entity entity = mc.getCameraEntity();
        BlockPos blockPos = state.blockPos;
        return entity != null && blockPos != null
               && entity.distanceToSqr(Vec3.atCenterOf(blockPos)) < OUTLINE_RENDER_DISTANCE;
    }
}
