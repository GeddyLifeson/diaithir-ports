package com.diaithir.addons.newage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleKineticRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import com.diaithir.addons.newage.content.heat.stirling.StirlingEngineBlockEntity;

/** Shaft plus the small flywheel driven by the engine's own animation angle. */
public class StirlingRenderer implements BlockEntityRenderer<StirlingEngineBlockEntity, StirlingRenderer.State> {
    private final ShaftRenderer shaft;

    public StirlingRenderer(BlockEntityRendererProvider.Context context) {
        this.shaft = new ShaftRenderer(context);
    }

    public static class State extends SingleKineticRenderState {
        SuperByteBufferRenderState wheel;
        org.joml.Quaternionf wheelAngle;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StirlingEngineBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   @Nullable CrumblingOverlay breakProgress) {
        shaft.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        Direction.Axis axis = be.getBlockState().getValue(BlockStateProperties.AXIS);
        BlockState wheelState = AllBlocks.FLYWHEEL.defaultBlockState().setValue(BlockStateProperties.AXIS, axis);
        state.wheel = CachedBuffers.block(wheelState)
                .translate(0.5f, 0.5f, 0.5f)
                .scale(0.2f, 0.2f, 0.2f)
                .translate(-0.5f, -0.5f, -0.5f)
                .cardinalLighting(be.getLevel())
                .light(state.lightCoords)
                .extractRenderState();
        float speed = be.getVisualSpeed(partialTicks) * 3 / 10f;
        float angle = be.getAngle() + speed * partialTicks;
        state.wheelAngle = KineticBlockEntityRenderer.getRotateAngle(angle, 0, axis);
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        shaft.submit(state, matrices, queue, camera);
        if (state.wheel != null) {
            matrices.pushPose();
            if (state.wheelAngle != null)
                matrices.rotateAround(state.wheelAngle, 0.5f, 0.5f, 0.5f);
            state.wheel.submit(matrices, queue);
            matrices.popPose();
        }
    }
}
