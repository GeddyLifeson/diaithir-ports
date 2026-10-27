package com.github.alexthe666.citadel.client.render.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/** Base for 1.21-style entity renderers that implement render(entity, yaw, partial, poseStack, buffer, light). */
public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyRenderState> {
    protected final EntityRendererProvider.Context context;

    protected LegacyEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.context = ctx;
    }

    @Override
    public LegacyRenderState createRenderState() {
        return new LegacyRenderState();
    }

    @Override
    public void extractRenderState(T entity, LegacyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.partialTick = partialTick;
    }

    public boolean isEntityUpsideDown(T entity) {
        return false;
    }

    protected float getWhiteOverlayProgress(T entity, float partialTicks) {
        return 0.0F;
    }

    public static int getOverlayCoords(net.minecraft.world.entity.LivingEntity entity, float whiteProgress) {
        return LegacyMobRenderer.getOverlayCoords(entity, whiteProgress);
    }

    /** 1.21 name kept so subclasses compile. */
    public abstract Identifier getTextureLocation(T entity);

    @SuppressWarnings("unchecked")
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void submit(LegacyRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.entity == null) return;
        LegacyRenderContext.begin(collector, camera, context);
        RecordingBufferSource rec = new RecordingBufferSource();
        T e = (T) state.entity;
        poseStack.pushPose();
        render(e, e.getYRot(), state.partialTick, poseStack, rec, state.lightCoords);
        poseStack.popPose();
        LegacyRenderContext.end();
        rec.flush(collector);
    }
}
