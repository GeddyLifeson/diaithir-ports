package net.diaithir.sharedspace.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.diaithir.sharedspace.Cells;
import net.diaithir.sharedspace.ClusterBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Draws each member block of a shared space at its quarter, with the normal block model and lighting. */
public final class ClusterRenderer implements BlockEntityRenderer<ClusterBlockEntity, ClusterRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        final MovingBlockRenderState[] cells = new MovingBlockRenderState[Cells.COUNT];
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ClusterBlockEntity be, State state, float partialTicks, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        for (int i = 0; i < Cells.COUNT; i++) {
            state.cells[i] = null;
            BlockState member = be.get(i);
            if (member != null && be.getLevel() instanceof ClientLevel level) {
                MovingBlockRenderState m = new MovingBlockRenderState();
                m.randomSeedPos = be.getBlockPos();
                m.blockPos = be.getBlockPos();
                m.blockState = member;
                m.biome = level.getBiome(be.getBlockPos());
                m.cardinalLighting = level.cardinalLighting();
                m.lightEngine = level.getLightEngine();
                state.cells[i] = m;
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < Cells.COUNT; i++) {
            if (state.cells[i] != null) {
                poseStack.pushPose();
                poseStack.translate(Cells.offsetX(i), 0.0, Cells.offsetZ(i));
                collector.submitMovingBlock(poseStack, state.cells[i]);
                poseStack.popPose();
            }
        }
    }
}
