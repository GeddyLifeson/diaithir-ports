package com.github.ysbbbbbb.kaleidoscopetavern.client.render.entity;

import com.github.ysbbbbbb.kaleidoscopetavern.entity.ThrownMolotovEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 投掷燃烧瓶的渲染器，将 MolotovBlock 作为旋转中的小方块渲染。
 */
public class ThrownMolotovRenderer extends EntityRenderer<ThrownMolotovEntity, EntityRenderState> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    /**
     * 每帧复用的 BlockModelRenderState，因为此渲染器只渲染一个方块模型
     */
    private final BlockModelRenderState blockModelRenderState = new BlockModelRenderState();

    public ThrownMolotovRenderer(EntityRendererProvider.Context context) {
        super(context);
        BlockState state = ModBlocks.MOLOTOV.get().defaultBlockState();
        context.getBlockModelResolver().update(blockModelRenderState, state, BLOCK_DISPLAY_CONTEXT);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();

        // 缩小方块模型，使其适合投射物大小
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(-0.5, 0, -0.5);

        // 飞行时旋转
        float rotation = state.ageInTicks * 20.0F;
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.7F));
        poseStack.translate(-0.5, -0.5, -0.5);

        // 更新并提交 Molotov 方块的模型
        blockModelRenderState.submit(poseStack, submitNodeCollector,
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
    }
}
