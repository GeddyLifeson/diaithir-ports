package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Renderer base for non-living entities written in the immediate-mode style.
 */
public abstract class ESEntityRenderer<T extends Entity> extends EntityRenderer<T, ESRenderState> {
	protected SubmitNodeCollector collector;
	protected ESEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ESRenderState createRenderState() {
		return new ESRenderState();
	}

	@Override
	public void extractRenderState(T entity, ESRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.entity = entity;
		state.partialTicks = partialTicks;
		state.entityYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void submit(ESRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		T entity = (T) state.entity;
		this.collector = collector;
		RecordingBufferSource.submit(collector, poseStack, source -> render(entity, state.entityYaw, state.partialTicks, poseStack, source, state.lightCoords));
		super.submit(state, poseStack, collector, camera);
	}

	/** Legacy immediate-mode hook. */
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
	}

	public Identifier getTextureLocation(T entity) {
		return null;
	}
}
