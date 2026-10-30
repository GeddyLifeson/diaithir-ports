package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarCabinetBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.BarCabinetRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BarCabinetBlockEntityRender implements BlockEntityRenderer<BarCabinetBlockEntity, BarCabinetRenderState> {
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver blockModelResolver;

    public BarCabinetBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public BarCabinetRenderState createRenderState() {
        return new BarCabinetRenderState();
    }

    @Override
    public void submit(BarCabinetRenderState state, PoseStack poseStack, SubmitNodeCollector submitNode, CameraRenderState camera) {
        Direction direction = state.direction;
        BlockModelRenderState leftBlockRender = state.leftBlockRender;
        BlockModelRenderState rightBlockRender = state.rightBlockRender;

        float scale = 0.9f;
        float angle = 180 - direction.get2DDataValue() * 90f;

        if (state.isSingle) {
            if (!leftBlockRender.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(0, 0.0625, 0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0, -0.5);
                leftBlockRender.submit(poseStack, submitNode, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        } else {
            if (!leftBlockRender.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(direction.getAxis() == Direction.Axis.Z ? 0.25 : -0.25, 0.0625, 0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0, -0.5);
                leftBlockRender.submit(poseStack, submitNode, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }

            if (!rightBlockRender.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(direction.getAxis() == Direction.Axis.Z ? -0.25 : 0.25, 0.0625, 0);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0, -0.5);
                rightBlockRender.submit(poseStack, submitNode, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }

    @Override
    public void extractRenderState(BarCabinetBlockEntity barCabinet, BarCabinetRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(barCabinet, state, partialTicks, cameraPosition, breakProgress);
        state.direction = barCabinet.getBlockState().getValue(BarCabinetBlock.FACING);
        state.isSingle = barCabinet.isSingle();

        ItemStack leftStack = barCabinet.getLeftItem();
        if (!leftStack.isEmpty() && leftStack.getItem() instanceof BlockItem blockItem) {
            this.blockModelResolver.update(state.leftBlockRender, blockItem.getBlock().defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
        } else {
            state.leftBlockRender.clear();
        }

        ItemStack rightStack = barCabinet.getRightItem();
        if (!rightStack.isEmpty() && rightStack.getItem() instanceof BlockItem blockItem) {
            this.blockModelResolver.update(state.rightBlockRender, blockItem.getBlock().defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
        } else {
            state.rightBlockRender.clear();
        }
    }
}
