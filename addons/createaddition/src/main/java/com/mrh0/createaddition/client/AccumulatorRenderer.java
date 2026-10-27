package com.mrh0.createaddition.client;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import com.mrh0.createaddition.index.CAPartials;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Four gauge faces with a swinging dial on top of a modular accumulator controller. */
public class AccumulatorRenderer implements BlockEntityRenderer<ModularAccumulatorBlockEntity, AccumulatorRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public boolean active;
        public float width, height, progress;
        public final List<SuperByteBufferRenderState[]> faces = new ArrayList<>();
        public final List<Float> yaws = new ArrayList<>();
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(ModularAccumulatorBlockEntity be, State s, float pt, Vec3 cam, @Nullable CrumblingOverlay crumbling) {
        SmartBlockEntityRenderer.extractBase(be, s, crumbling);
        s.faces.clear(); s.yaws.clear();
        s.active = be.isController();
        if (!s.active) return;
        s.width = be.getWidth(); s.height = be.getHeight();
        s.progress = be.gauge.getValue(pt);
        BlockState bs = be.getBlockState();
        for (Direction d : new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST}) {
            s.faces.add(new SuperByteBufferRenderState[]{
                CachedBuffers.partial(CAPartials.ACCUMULATOR_GUAGE, bs).light(s.lightCoords).extractRenderState(),
                CachedBuffers.partial(CAPartials.ACCUMULATOR_DIAL, bs).light(s.lightCoords).extractRenderState()});
            s.yaws.add(d.toYRot());
        }
        be.observe();
    }

    @Override
    public void submit(State s, PoseStack ms, SubmitNodeCollector q, CameraRenderState cam) {
        if (!s.active) return;
        ms.pushPose();
        ms.translate(s.width / 2f, s.height - 0.5f, s.width / 2f);
        float py = 6f / 16f, pz = 8f / 16f;
        for (int i = 0; i < s.faces.size(); i++) {
            ms.pushPose();
            ms.mulPose(Axis.YP.rotationDegrees(s.yaws.get(i)));
            ms.translate(-0.5f, -0.5f, -0.5f);
            ms.translate(s.width / 2f - 6 / 16f, 0, 0);
            s.faces.get(i)[0].submit(ms, q);
            ms.translate(0, py, pz);
            ms.mulPose(Axis.XP.rotationDegrees(-180 * s.progress));
            ms.translate(0, -py, -pz);
            s.faces.get(i)[1].submit(ms, q);
            ms.popPose();
        }
        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() { return true; }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(ModularAccumulatorBlockEntity be) {
        var p = be.getBlockPos();
        return be.isController() ? new net.minecraft.world.phys.AABB(p).expandTowards(be.getWidth() - 1, be.getHeight() - 1, be.getWidth() - 1) : new net.minecraft.world.phys.AABB(p);
    }
}
