package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.entity.misc.ESFallingBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class ESFallingBlockRenderer extends EntityRenderer<ESFallingBlock, FallingBlockRenderState> {
	public ESFallingBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
	}

	@Override
	public void submit(FallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		BlockState blockState = state.movingBlockRenderState.blockState;
		if (blockState != null && blockState.getRenderShape() == RenderShape.MODEL) {
			poseStack.pushPose();
			poseStack.translate(-0.5, 0.0, -0.5);
			collector.submitMovingBlock(poseStack, state.movingBlockRenderState);
			poseStack.popPose();
			super.submit(state, poseStack, collector, camera);
		}
	}

	@Override
	public FallingBlockRenderState createRenderState() {
		return new FallingBlockRenderState();
	}

	@Override
	public void extractRenderState(ESFallingBlock entity, FallingBlockRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		state.movingBlockRenderState.randomSeedPos = entity.getStartPos();
		state.movingBlockRenderState.blockPos = pos;
		state.movingBlockRenderState.blockState = entity.getBlockState();
		if (entity.level() instanceof ClientLevel clientLevel) {
			state.movingBlockRenderState.biome = clientLevel.getBiome(pos);
			state.movingBlockRenderState.cardinalLighting = clientLevel.cardinalLighting();
			state.movingBlockRenderState.lightEngine = clientLevel.getLightEngine();
		}
	}
}
