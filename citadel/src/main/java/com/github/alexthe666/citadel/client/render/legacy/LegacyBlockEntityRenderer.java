package com.github.alexthe666.citadel.client.render.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Base for 1.21-style block entity renderers implementing render(be, partial, poseStack, buffer, light, overlay). */
public abstract class LegacyBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, LegacyBlockEntityRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public BlockEntity blockEntity;
        public float partialTick;
    }

    protected final BlockEntityRendererProvider.Context context;

    protected LegacyBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.context = ctx;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T be, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.blockEntity = be;
        state.partialTick = partialTicks;
    }

    public abstract void render(T be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay);

    @SuppressWarnings("unchecked")
    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.blockEntity == null) return;
        LegacyRenderContext.begin(collector, camera, null);
        RecordingBufferSource rec = new RecordingBufferSource();
        poseStack.pushPose();
        try {
            render((T) state.blockEntity, state.partialTick, poseStack, rec, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        } finally {
            poseStack.popPose();
            LegacyRenderContext.end();
        }
        rec.flush(collector);
    }
}
