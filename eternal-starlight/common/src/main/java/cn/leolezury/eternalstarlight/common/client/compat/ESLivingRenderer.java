package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Renderer base for living entities that keeps the old entity based hooks.
 */
public abstract class ESLivingRenderer<T extends LivingEntity, M extends ESModel<T>> extends LivingEntityRenderer<T, ESRenderState, M> {
	protected SubmitNodeCollector collector;
	protected CameraRenderState camera;
	private ESRenderState currentState;

	protected ESLivingRenderer(EntityRendererProvider.Context context, M model, float shadow) {
		super(context, model, shadow);
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
	public final Identifier getTextureLocation(ESRenderState state) {
		return getTextureLocation(cast(state));
	}

	public abstract Identifier getTextureLocation(T entity);

	@SuppressWarnings("unchecked")
	protected T cast(ESRenderState state) {
		return (T) state.entity;
	}

	@Override
	public void submit(ESRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		currentState = state;
		super.submit(state, poseStack, collector, camera);
		this.collector = collector;
		this.camera = camera;
		RecordingBufferSource.submit(collector, poseStack, source -> render(cast(state), state.entityYaw, state.partialTicks, poseStack, source, state.lightCoords));
	}

	/** Legacy hook for extra immediate-mode drawing, run after the model. */
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
	}

	@Override
	protected final void scale(ESRenderState state, PoseStack poseStack) {
		scale(cast(state), poseStack, state.partialTicks);
	}

	protected void scale(T entity, PoseStack poseStack, float partialTick) {
	}

	@Override
	protected final float getWhiteOverlayProgress(ESRenderState state) {
		return getWhiteOverlayProgress(cast(state), state.partialTicks);
	}

	protected float getWhiteOverlayProgress(T entity, float partialTick) {
		return 0.0F;
	}

	@Override
	protected final void setupRotations(ESRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
		currentState = state;
		setupRotations(cast(state), poseStack, state.ageInTicks, bodyRot, state.partialTicks, entityScale);
	}

	protected void setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
		super.setupRotations(currentState, poseStack, yBodyRot, scale);
	}

	@Override
	protected final float getFlipDegrees() {
		return currentState != null ? getFlipDegrees(cast(currentState)) : super.getFlipDegrees();
	}

	protected float getFlipDegrees(T entity) {
		return super.getFlipDegrees();
	}

	@Override
	protected final float getShadowRadius(ESRenderState state) {
		return state.entity != null ? getShadowRadius(cast(state)) : super.getShadowRadius(state);
	}

	protected float getShadowRadius(T entity) {
		return this.shadowRadius;
	}

	protected boolean isBodyVisible(T entity) {
		return !entity.isInvisible();
	}
}
