package org.diaithir.connected.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Non-instanced fallback for {@link SplitBoxVisual}. */
public class SplitBoxRenderer<T extends SplitShaftBlockEntity> implements BlockEntityRenderer<T, SplitBoxRenderer.State> {
    private final SplitBoxVisual.Modifier modifier;

    public SplitBoxRenderer(SplitBoxVisual.Modifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T be, State state, float tickProgress, Vec3 cameraPos, @Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, state, tickProgress, cameraPos, crumbling);
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumbling);
        CardinalLighting lighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.models.clear();
        state.angles.clear();
        Direction.Axis boxAxis = state.blockState.getValue(BlockStateProperties.AXIS);
        Direction sourceFacing = null;
        if (be.hasSource() && be.getSpeed() != 0) {
            BlockPos src = be.source.subtract(state.blockPos);
            sourceFacing = Direction.getApproximateNearest(src.getX(), src.getY(), src.getZ());
        }
        float progress = KineticBlockEntityRenderer.getProgress(be.getSpeed(), level);
        int color = KineticBlockEntityRenderer.getTintColor(be);
        for (Direction direction : Iterate.directions) {
            if (direction.getAxis() == boxAxis) continue;
            float mod = sourceFacing == null ? 1 : modifier.of(state.blockState, direction, sourceFacing);
            float offset = KineticBlockEntityVisual.rotationOffset(state.blockState, direction.getAxis(), state.blockPos);
            state.angles.add(KineticBlockEntityRenderer.getRotateAngle(progress * mod, offset, direction.getAxis()));
            state.models.add(CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, direction)
                .cardinalLighting(lighting).light(state.lightCoords).color(color).extractRenderState());
        }
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        for (int i = 0; i < state.models.size(); i++) {
            matrices.pushPose();
            matrices.rotateAround(state.angles.get(i), 0.5f, 0.5f, 0.5f);
            state.models.get(i).submit(matrices, queue);
            matrices.popPose();
        }
    }

    public static class State extends BlockEntityRenderState {
        public final List<SuperByteBufferRenderState> models = new ArrayList<>();
        public final List<Quaternionf> angles = new ArrayList<>();
    }
}
