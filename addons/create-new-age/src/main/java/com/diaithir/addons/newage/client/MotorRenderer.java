package com.diaithir.addons.newage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleKineticRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Half shaft sticking out of a motor (same rendering as the Create creative motor). */
public class MotorRenderer<T extends KineticBlockEntity> implements BlockEntityRenderer<T, SingleKineticRenderState> {
    public MotorRenderer(Context context) {
    }

    @Override
    public SingleKineticRenderState createRenderState() {
        return new SingleKineticRenderState();
    }

    @Override
    public void extractRenderState(T be, SingleKineticRenderState state, float partialTicks, Vec3 cameraPosition,
                                   @Nullable CrumblingOverlay breakProgress) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING);
        state.model = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), facing)
                .cardinalLighting(level)
                .light(state.lightCoords)
                .color(KineticBlockEntityRenderer.getTintColor(be))
                .extractRenderState();
        state.angle = KineticBlockEntityRenderer.getRotateAngleWithoutBeOffset(facing.getAxis(), be, state, level);
    }

    @Override
    public void submit(SingleKineticRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        state.submit(matrices, queue);
    }
}
