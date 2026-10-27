package com.mrh0.createaddition.client;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

/** Renders the turning shaft stubs of the electric motor, the alternator and the rolling mill. */
public class ShaftMachineRenderer<T extends KineticBlockEntity> implements BlockEntityRenderer<T, ShaftLayersState> {
    public enum Kind { MOTOR, ALTERNATOR, ROLLING_MILL }

    private final Kind kind;

    public ShaftMachineRenderer(Kind kind) {
        this.kind = kind;
    }

    @Override
    public ShaftLayersState createRenderState() {
        return new ShaftLayersState();
    }

    @Override
    public void extractRenderState(T be, ShaftLayersState state, float partialTicks, Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        state.layers.clear();
        int tint = getTintColor(be);
        switch (kind) {
            case MOTOR -> {
                Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING);
                state.layers.add(new ShaftLayersState.Layer(
                    CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), facing).cardinalLighting(level).light(state.lightCoords).color(tint).extractRenderState(),
                    null, 0, getRotateAngleWithoutBeOffset(facing.getAxis(), be, state, level)));
            }
            case ALTERNATOR -> {
                Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING);
                var spin = getRotateAngleWithoutBeOffset(facing.getAxis(), be, state, level);
                for (Direction d : new Direction[]{facing, facing.getOpposite()}) {
                    state.layers.add(new ShaftLayersState.Layer(
                        CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), d).cardinalLighting(level).light(state.lightCoords).color(tint).extractRenderState(),
                        null, 0, spin));
                }
            }
            case ROLLING_MILL -> {
                Axis axis = getRotationAxisOf(be);
                state.layers.add(new ShaftLayersState.Layer(
                    CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(level).light(state.lightCoords).color(tint).extractRenderState(),
                    null, 0, getRotateAngleWithoutBeOffset(axis, be, state, level)));
                float angle = getAngleForBe(be, be.getBlockPos(), axis);
                float yaw1 = axis == Axis.Z ? 0 : RAD_90;
                float yaw2 = axis == Axis.Z ? RAD_180 : RAD_270;
                state.layers.add(new ShaftLayersState.Layer(
                    CachedBuffers.partial(AllPartialModels.SHAFT_HALF, be.getBlockState()).cardinalLighting(level).light(state.lightCoords).color(tint).extractRenderState(),
                    getRadiansRotateAngle(yaw1, Direction.UP), 4f / 16f, getRadiansRotateAngle(angle, Direction.NORTH)));
                state.layers.add(new ShaftLayersState.Layer(
                    CachedBuffers.partial(AllPartialModels.SHAFT_HALF, be.getBlockState()).cardinalLighting(level).light(state.lightCoords).color(tint).extractRenderState(),
                    getRadiansRotateAngle(yaw2, Direction.UP), 4f / 16f, getRadiansRotateAngle(-angle, Direction.NORTH)));
            }
        }
    }

    @Override
    public void submit(ShaftLayersState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        state.submit(matrices, queue);
    }
}
