package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Particle group that lets particles write their own geometry (old-style render(VertexConsumer, Camera, float)).
 */
public class ESParticleGroup extends ParticleGroup<ESTextureSheetParticle> {
	private final State state = new State();

	public ESParticleGroup(ParticleEngine engine) {
		super(engine);
	}

	@Override
	public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
		state.clear();
		state.camera = camera;
		state.partialTick = partialTickTime;
		for (ESTextureSheetParticle particle : this.particles) {
			if (frustum.pointInFrustum(particle.getX(), particle.getY(), particle.getZ())) {
				state.visible.add(particle);
			}
		}
		return state;
	}

	private static class State implements ParticleGroupRenderState {
		private final List<ESTextureSheetParticle> visible = new ArrayList<>();
		private Camera camera;
		private float partialTick;

		@Override
		public void submit(SubmitNodeCollector collector, CameraRenderState cameraState) {
			if (visible.isEmpty() || camera == null) return;
			Map<RenderType, List<ESTextureSheetParticle>> byType = new LinkedHashMap<>();
			for (ESTextureSheetParticle particle : visible) {
				byType.computeIfAbsent(particle.getESRenderType(), k -> new ArrayList<>()).add(particle);
			}
			PoseStack poseStack = new PoseStack();
			for (Map.Entry<RenderType, List<ESTextureSheetParticle>> entry : byType.entrySet()) {
				List<ESTextureSheetParticle> list = entry.getValue();
				Camera cam = camera;
				float pt = partialTick;
				collector.submitCustomGeometry(poseStack, entry.getKey(), (pose, consumer) -> {
					for (ESTextureSheetParticle particle : list) {
						particle.render(consumer, cam, pt);
					}
				});
			}
		}

		@Override
		public void clear() {
			visible.clear();
		}
	}
}
