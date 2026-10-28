package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * Adapter that runs the original (pre-26.1 style) PneumaticCraft entity renderers on the submit API,
 * using the same recording approach as {@link LegacyBER}.
 */
public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyEntityRenderer.State<T>> {
    public static class State<T extends Entity> extends EntityRenderState {
        public T entity;
        public float yaw;
    }

    protected LegacyEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public State<T> createRenderState() {
        return new State<>();
    }

    @Override
    public void extractRenderState(T entity, State<T> state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.yaw = entity.getYRot();
    }

    @Override
    public void submit(State<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.entity == null || state.entity.isRemoved()) return;
        LegacyBER.Recorder rec = new LegacyBER.Recorder(state.lightCoords);
        render(state.entity, state.yaw, state.partialTick, poseStack, rec, state.lightCoords);
        rec.replay(collector);
        super.submit(state, poseStack, collector, camera);
    }

    protected abstract void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light);

    public Identifier getTextureLocation(T entity) {
        return null;
    }
}
