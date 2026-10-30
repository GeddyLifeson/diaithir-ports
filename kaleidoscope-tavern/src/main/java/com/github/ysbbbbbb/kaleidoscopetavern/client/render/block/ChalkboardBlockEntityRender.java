package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.ChalkboardBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.model.deco.LargeChalkboardModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.model.deco.SmallChalkboardModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.TextBlockRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class ChalkboardBlockEntityRender extends TextBlockEntityRender<ChalkboardBlockEntity, TextBlockRenderState> {
    private static final Identifier SMALL_TEXTURE = KaleidoscopeTavern.modLoc("textures/entity/deco/small_chalkboard.png");
    private static final Identifier LARGE_TEXTURE = KaleidoscopeTavern.modLoc("textures/entity/deco/large_chalkboard.png");

    private static final float TEXT_SCALE = 0.012f;
    private static final int LINE_HEIGHT = 12;
    private static final int MAX_LINES = 11;

    private final SmallChalkboardModel small;
    private final SmallChalkboardModel large;

    public ChalkboardBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
        this.small = new SmallChalkboardModel(context.bakeLayer(SmallChalkboardModel.LAYER_LOCATION));
        this.large = new SmallChalkboardModel(context.bakeLayer(LargeChalkboardModel.LAYER_LOCATION));
    }

    @Override
    public TextBlockRenderState createRenderState() {
        return new TextBlockRenderState();
    }

    @Override
    public void extractRenderState(ChalkboardBlockEntity textBlock, TextBlockRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(textBlock, state, partialTicks, cameraPosition, breakProgress);
        state.isLarge = textBlock.isLarge();
    }

    @Override
    public void submit(TextBlockRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        Direction facing = state.facing;
        int light = state.lightCoords;
        boolean isLarge = state.isLarge;

        // 黑板模型
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(180 - facing.get2DDataValue() * 90));

        if (isLarge) {
            submitNodeCollector.submitModel(large, Unit.INSTANCE, poseStack, LARGE_TEXTURE,
                    light, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        } else {
            submitNodeCollector.submitModel(small, Unit.INSTANCE, poseStack, SMALL_TEXTURE,
                    light, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        }
        poseStack.popPose();

        // 渲染文字（若有内容）
        if (StringUtils.isNotBlank(state.text)) {
            poseStack.pushPose();

            if (facing == Direction.EAST) {
                poseStack.translate(0.08, 1.535, 0.5);
            } else if (facing == Direction.WEST) {
                poseStack.translate(0.92, 1.535, 0.5);
            } else if (facing == Direction.SOUTH) {
                poseStack.translate(0.5, 1.535, 0.08);
            } else {
                poseStack.translate(0.5, 1.535, 0.92);
            }
            poseStack.mulPose(Axis.YN.rotationDegrees(facing.get2DDataValue() * 90));

            int maxWidth = isLarge ? 232 : 63;
            doTextRender(state, poseStack, submitNodeCollector, state.text, maxWidth, TEXT_SCALE, MAX_LINES, LINE_HEIGHT);

            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(ChalkboardBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        if (blockEntity.isLarge()) {
            return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1,
                    pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
        }
        return new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }
}
