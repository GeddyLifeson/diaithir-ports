package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Layer base keeping the old immediate-mode signature.
 */
public abstract class ESRenderLayer<T extends LivingEntity, M extends EntityModel<ESRenderState>> extends RenderLayer<ESRenderState, M> {
	protected SubmitNodeCollector collector;
	protected ESRenderState state;

	protected ESRenderLayer(RenderLayerParent<ESRenderState, M> parent) {
		super(parent);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, ESRenderState state, float yRot, float xRot) {
		T entity = (T) state.entity;
		this.collector = collector;
		this.state = state;
		RecordingBufferSource.submit(collector, poseStack, source -> render(poseStack, source, lightCoords, entity, state.walkAnimationPos, state.walkAnimationSpeed, state.partialTicks, state.ageInTicks, yRot, xRot));
	}

	public abstract void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch);
}
