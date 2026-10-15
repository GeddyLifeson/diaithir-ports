package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.render.legacy.RecordingBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;

/**
 * Entity renderer for code written against the pre-26.1 render call. The entity is kept in the render state
 * (extraction and submission both happen on the render thread) and the old render call draws into a
 * {@link RecordingBufferSource}, which is then submitted. Same idea as {@code LegacyBlockEntityRenderer}.
 */
public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyEntityRenderer.State<T>> {
    public static class State<T> extends EntityRenderState {
        public T entity;
    }

    protected LegacyEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    protected abstract void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight);

    @Override
    public State<T> createRenderState() {
        return new State<>();
    }

    @Override
    public void extractRenderState(T entity, State<T> state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entity = entity;
    }

    @Override
    public void submit(State<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RecordingBufferSource buffer = new RecordingBufferSource(collector);
        render(state.entity, state.entity.getYRot(), state.partialTick, poseStack, buffer, state.lightCoords);
        buffer.submit();
        super.submit(state, poseStack, collector, camera);
    }
}
