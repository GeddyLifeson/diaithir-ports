/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.proxy.client.render;

import com.buuz135.industrial.block.transportstorage.ConveyorBlock;
import com.buuz135.industrial.block.transportstorage.tile.ConveyorTile;
import com.buuz135.industrial.api.conveyor.ConveyorUpgrade;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FluidConveyorTESR implements BlockEntityRenderer<ConveyorTile, FluidConveyorTESR.State> {

    public FluidConveyorTESR(BlockEntityRendererProvider.Context p_173540_) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ConveyorTile te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);
        state.upgradeModels.clear();
        Direction conveyorFacing = te.getBlockState().getValue(ConveyorBlock.FACING);
        for (ConveyorUpgrade upgrade : te.getUpgradeMap().values()) {
            if (upgrade != null) state.upgradeModels.add(upgrade.getFactory().getModel(upgrade.getSide(), conveyorFacing));
        }
        FluidStack fluid = te.getTank().getFluid();
        state.still = null;
        if (fluid.isEmpty()) return;
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
        state.still = model.stillMaterial().sprite();
        state.flow = model.flowingMaterial().sprite();
        state.color = model.fluidTintSource() == null ? -1 : model.fluidTintSource().colorAsStack(fluid);
        state.facing = te.getFacing();
        state.renderPrev = !(te.getLevel().getBlockEntity(te.getBlockPos().relative(state.facing.getOpposite())) instanceof ConveyorTile prev) || prev.getTank().getFluidAmount() <= 0;
        state.renderNext = !(te.getLevel().getBlockEntity(te.getBlockPos().relative(state.facing)) instanceof ConveyorTile next) || next.getTank().getFluidAmount() <= 0;
    }

    @Override
    public void submit(State state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Identifier model : state.upgradeModels) {
            UpgradeModels.submit(model, matrixStack, collector, state.lightCoords);
        }
        if (state.still == null) return;
        matrixStack.pushPose();
        if (state.facing == Direction.NORTH) {
            matrixStack.translate(1, 0, 1);
            matrixStack.mulPose(Axis.YP.rotationDegrees(180));
        }
        if (state.facing == Direction.EAST) {
            matrixStack.translate(0, 0, 1);
            matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        if (state.facing == Direction.WEST) {
            matrixStack.translate(1, 0, 0);
            matrixStack.mulPose(Axis.YN.rotationDegrees(90));
        }
        collector.submitCustomGeometry(matrixStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
            float posY = 2 / 16f - 1 / 32f;
            float right = 1 / 16f;
            float left = 15 / 16f;
            TextureAtlasSprite flow = state.flow;
            TextureAtlasSprite still = state.still;
            vertex(buffer, pose, state, left, posY, 0, flow.getU0(), flow.getV0(), 0, 1, 0);
            vertex(buffer, pose, state, right, posY, 0, flow.getU(0.5f), flow.getV0(), 0, 1, 0);
            vertex(buffer, pose, state, right, posY, 1, flow.getU(0.5f), flow.getV(0.5f), 0, 1, 0);
            vertex(buffer, pose, state, left, posY, 1, flow.getU0(), flow.getV(0.5f), 0, 1, 0);
            float edgeV = still.getV(15 / 16f);
            if (state.renderPrev) {
                vertex(buffer, pose, state, right, posY, 0, still.getU0(), edgeV, 0, 0, -1);
                vertex(buffer, pose, state, left, posY, 0, still.getU1(), edgeV, 0, 0, -1);
                vertex(buffer, pose, state, left, 1 / 16f, 0, still.getU1(), still.getV1(), 0, 0, -1);
                vertex(buffer, pose, state, right, 1 / 16f, 0, still.getU0(), still.getV1(), 0, 0, -1);
            }
            if (state.renderNext) {
                vertex(buffer, pose, state, left, posY, 1, still.getU1(), edgeV, 0, 0, 1);
                vertex(buffer, pose, state, right, posY, 1, still.getU0(), edgeV, 0, 0, 1);
                vertex(buffer, pose, state, right, 1 / 16f, 1, still.getU0(), still.getV1(), 0, 0, 1);
                vertex(buffer, pose, state, left, 1 / 16f, 1, still.getU1(), still.getV1(), 0, 0, 1);
            }
        });
        matrixStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, State state, float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setColor(state.color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(pose, nx, ny, nz);
    }

    public static class State extends BlockEntityRenderState {
        final List<Identifier> upgradeModels = new ArrayList<>();
        @Nullable TextureAtlasSprite still;
        TextureAtlasSprite flow;
        int color;
        Direction facing;
        boolean renderPrev;
        boolean renderNext;
    }
}
