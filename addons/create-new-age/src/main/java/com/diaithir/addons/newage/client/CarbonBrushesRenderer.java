package com.diaithir.addons.newage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleKineticRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
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

import com.diaithir.addons.newage.content.electricity.generation.brushes.CarbonBrushesBlockEntity;

/** Shaft through the brushes plus the spinning coil. */
public class CarbonBrushesRenderer implements BlockEntityRenderer<CarbonBrushesBlockEntity, CarbonBrushesRenderer.State> {
    private final ShaftRenderer shaft;

    public CarbonBrushesRenderer(BlockEntityRendererProvider.Context context) {
        this.shaft = new ShaftRenderer(context);
    }

    public static class State extends SingleKineticRenderState {
        SuperByteBufferRenderState coil;
        org.joml.Quaternionf coilAngle;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CarbonBrushesBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   @Nullable CrumblingOverlay breakProgress) {
        shaft.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        BlockState bs = be.getBlockState();
        Direction dir = bs.getValue(BlockStateProperties.FACING);
        var level = be.getLevel();
        var buf = CachedBuffers.partial(CNAPartialModels.COIL, bs);
        switch (dir.getAxis()) {
            case X -> buf.rotateCentered((float) Math.toRadians(90), Direction.Axis.Z);
            case Z -> buf.rotateCentered((float) Math.toRadians(90), Direction.Axis.X);
            default -> {
            }
        }
        state.coil = buf.cardinalLighting(level).light(state.lightCoords).extractRenderState();
        state.coilAngle = KineticBlockEntityRenderer.getRotateAngleWithoutBeOffset(dir.getAxis(), be, state, level);
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        shaft.submit(state, matrices, queue, camera);
        if (state.coil != null) {
            matrices.pushPose();
            if (state.coilAngle != null)
                matrices.rotateAround(state.coilAngle, 0.5f, 0.5f, 0.5f);
            state.coil.submit(matrices, queue);
            matrices.popPose();
        }
    }
}
