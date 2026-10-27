package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity renderer base keeping the old immediate-mode signature.
 */
public abstract class ESBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ESBlockEntityRenderer.State> {
	protected SubmitNodeCollector collector;

	public static class State extends BlockEntityRenderState {
		public BlockEntity blockEntity;
		public float partialTicks;
		public Vec3 cameraPos = Vec3.ZERO;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
		state.blockEntity = blockEntity;
		state.partialTicks = partialTicks;
		state.partialTicks = partialTicks;
		state.cameraPos = cameraPosition;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		T be = (T) state.blockEntity;
		this.collector = collector;
		RecordingBufferSource.submit(collector, poseStack, source -> render(be, state.partialTicks, poseStack, source, state.lightCoords, OverlayTexture.NO_OVERLAY));
	}

	public abstract void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
}
