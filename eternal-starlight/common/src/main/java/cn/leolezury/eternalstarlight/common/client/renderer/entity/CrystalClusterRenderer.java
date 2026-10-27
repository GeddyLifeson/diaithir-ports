package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.entity.attack.CrystalCluster;
import cn.leolezury.eternalstarlight.common.registry.ESBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class CrystalClusterRenderer extends EntityRenderer<CrystalCluster, CrystalClusterRenderer.State> {
	public static class State extends FallingBlockRenderState {
		public float scale;
		public float yaw;
		public boolean visible;
	}

	public CrystalClusterRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.2F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CrystalCluster cluster, State state, float partialTicks) {
		super.extractRenderState(cluster, state, partialTicks);
		BlockState blockState = cluster.clientCrystalType == 1 ? ESBlocks.RED_STARLIGHT_CRYSTAL_CLUSTER.get().defaultBlockState() : ESBlocks.BLUE_STARLIGHT_CRYSTAL_CLUSTER.get().defaultBlockState();
		BlockPos pos = BlockPos.containing(cluster.getX(), cluster.getBoundingBox().maxY, cluster.getZ());
		state.visible = blockState.getRenderShape() == RenderShape.MODEL && blockState != cluster.level().getBlockState(cluster.blockPosition());
		state.scale = Math.max(Mth.lerp(partialTicks, cluster.oldClientScale, cluster.clientScale), 0);
		state.yaw = Mth.rotLerp(partialTicks, cluster.yRotO, cluster.getYRot());
		state.movingBlockRenderState.randomSeedPos = cluster.blockPosition();
		state.movingBlockRenderState.blockPos = pos;
		state.movingBlockRenderState.blockState = blockState;
		if (cluster.level() instanceof ClientLevel clientLevel) {
			state.movingBlockRenderState.biome = clientLevel.getBiome(pos);
			state.movingBlockRenderState.cardinalLighting = clientLevel.cardinalLighting();
			state.movingBlockRenderState.lightEngine = clientLevel.getLightEngine();
		}
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.visible) {
			stack.pushPose();
			stack.mulPose(new Quaternionf().rotateY(-state.yaw * Mth.DEG_TO_RAD));
			stack.translate(-0.5 * state.scale, 0.0, -0.5 * state.scale);
			stack.scale(state.scale, state.scale, state.scale);
			collector.submitMovingBlock(stack, state.movingBlockRenderState);
			stack.popPose();
			super.submit(state, stack, collector, camera);
		}
	}
}
