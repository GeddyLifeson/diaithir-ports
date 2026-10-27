package com.diaithir.addons.newage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleKineticRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import com.diaithir.addons.newage.content.energising.EnergiserBlockEntity;

/** Shaft plus the charging beam shown while an item is being energised. */
public class EnergiserRenderer implements net.minecraft.client.renderer.blockentity.BlockEntityRenderer<EnergiserBlockEntity, EnergiserRenderer.State> {
    private final ShaftRenderer shaft;

    public EnergiserRenderer(BlockEntityRendererProvider.Context context) {
        this.shaft = new ShaftRenderer(context);
    }

    public static class State extends SingleKineticRenderState {
        @Nullable SuperByteBufferRenderState beam;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EnergiserBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   @Nullable CrumblingOverlay breakProgress) {
        shaft.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.beam = null;
        if (be.size > 0f && be.getLevel() != null) {
            float scalar = (1 - be.size * 0.12f) * 0.5f;
            state.beam = CachedBuffers.block(Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState())
                    .translate(scalar, -1.2f, scalar)
                    .scale(be.size * 0.12f, 1.3f, be.size * 0.12f)
                    .light(0xF000F0)
                    .disableDiffuse()
                    .extractRenderState();
        }
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        if (state.beam != null) {
            matrices.pushPose();
            state.beam.submit(matrices, queue);
            matrices.popPose();
        }
        shaft.submit(state, matrices, queue, camera);
    }
}
