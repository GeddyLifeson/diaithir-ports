package com.github.ysbbbbbb.kaleidoscopetavern.client.render.block;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.model.brew.BarrelModel;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.BarrelRenderState;
import com.github.ysbbbbbb.kaleidoscopetavern.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

import static com.github.ysbbbbbb.kaleidoscopetavern.util.RenderUtils.stableRandom;

public class BarrelBlockEntityRender implements BlockEntityRenderer<BarrelBlockEntity, BarrelRenderState> {
    private static final Identifier LARGE_TEXTURE = KaleidoscopeTavern.modLoc("textures/entity/brew/barrel.png");

    private final ItemModelResolver itemModelResolver;
    private final BarrelModel model;

    public BarrelBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.model = new BarrelModel(context.bakeLayer(BarrelModel.LAYER_LOCATION));
    }

    @Override
    public BarrelRenderState createRenderState() {
        return new BarrelRenderState();
    }

    @Override
    public void extractRenderState(BarrelBlockEntity barrel, BarrelRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(barrel, state, partialTicks, cameraPosition, breakProgress);
        state.direction = barrel.getBlockState().getValue(BarrelBlock.FACING);
        state.isOpen = barrel.isOpen();
        var fluidResource = barrel.getFluid().getResource(0);
        state.fluid = fluidResource.isEmpty() ? null : fluidResource.getFluid();
        state.fluidAmount = barrel.getFluid().getAmountAsInt(0);
        state.blockPosSeed = barrel.getBlockPos().asLong();

        Level level = barrel.getLevel();
        if (level != null) {
            state.gameTime = level.getGameTime();
            state.partialTicks = partialTicks;
        }

        // 提取物品渲染状态
        ItemStacksResourceHandler items = barrel.getIngredient();
        int globalIndex = 0;
        for (int index = 0; index < items.size(); index++) {
            var itemResource = items.getResource(index);
            ItemStack stack = itemResource.isEmpty() ? ItemStack.EMPTY : itemResource.toStack(items.getAmountAsInt(index));
            if (!stack.isEmpty()) {
                int count = stack.getCount() / 2 + 1;
                for (int i = 0; i < count && globalIndex < BarrelRenderState.MAX_RENDER_ITEMS; i++) {
                    this.itemModelResolver.updateForTopItem(state.itemRenders[globalIndex], stack,
                            ItemDisplayContext.FIXED, level, null, 0);
                    globalIndex++;
                }
            }
        }
        state.itemCount = globalIndex;

        // 清除未使用的 render state
        for (int i = globalIndex; i < BarrelRenderState.MAX_RENDER_ITEMS; i++) {
            state.itemRenders[i].clear();
        }
    }

    @Override
    public void submit(BarrelRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        // 渲染桶本体
        this.renderBody(state, poseStack, submitNodeCollector);

        // 开盖后渲染内容物
        if (state.isOpen) {
            if (state.fluidAmount > 0) {
                this.renderFluid(state, poseStack, submitNodeCollector);
            }
            this.renderItems(state, poseStack, submitNodeCollector);
        }
    }

    private void renderBody(BarrelRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Direction facing = state.direction;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(180 - facing.get2DDataValue() * 90));

        submitNodeCollector.submitModel(this.model, state, poseStack, LARGE_TEXTURE,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);

        poseStack.popPose();
    }

    private void renderFluid(BarrelRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Fluid fluid = state.fluid;
        if (fluid == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0, 2, 0);

        float percent = state.fluidAmount / (float) IBarrel.MAX_FLUID_AMOUNT;
        float y = percent * 0.65f;

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(),
                (pose, vertexConsumer) -> RenderUtils.renderFluid(fluid, pose, vertexConsumer,
                        state.lightCoords, 16, y));

        poseStack.popPose();
    }

    private void renderItems(BarrelRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        float time = state.gameTime + state.partialTicks;
        long seed = state.blockPosSeed;

        for (int i = 0; i < state.itemCount; i++) {
            ItemStackRenderState itemRender = state.itemRenders[i];
            if (itemRender.isEmpty()) {
                continue;
            }

            poseStack.pushPose();

            float x = stableRandom(seed, i, i + 1) * 0.4f;
            float z = stableRandom(seed, i, i + 2) * 0.4f;
            float y = (float) (i / 4) * 0.025f + stableRandom(seed, i, i + 3) * 0.05f;

            // 添加一些上下浮动效果
            y += (float) (Math.sin(time / 10f + i) * 0.02f);

            float yRot = stableRandom(seed, i, i + 4) * 5f;
            float zRot = stableRandom(seed, i, i + 5) * 360f;

            poseStack.translate(0.5f + x, 2.7f + y, 0.5f + z);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            poseStack.mulPose(Axis.YN.rotationDegrees(yRot));
            poseStack.mulPose(Axis.ZN.rotationDegrees(zRot));

            itemRender.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(BarrelBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 2, pos.getY(), pos.getZ() - 2,
                pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
    }
}
