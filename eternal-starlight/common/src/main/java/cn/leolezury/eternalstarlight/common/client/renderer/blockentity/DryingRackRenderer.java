package cn.leolezury.eternalstarlight.common.client.renderer.blockentity;

import cn.leolezury.eternalstarlight.common.client.compat.ESRender;
import cn.leolezury.eternalstarlight.common.client.compat.ESBlockEntityRenderer;
import cn.leolezury.eternalstarlight.common.block.DryingRackBlock;
import cn.leolezury.eternalstarlight.common.block.entity.DryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;

public class DryingRackRenderer extends ESBlockEntityRenderer<DryingRackBlockEntity> {
	public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(DryingRackBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.75F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getBlockState().getValue(DryingRackBlock.FACING).toYRot()));
		poseStack.translate(0, 0, 0.01F);
		poseStack.scale(0.6F, 0.6F, 0.6F);
		ESRender.submitItem(this.collector, blockEntity.getItem(), ItemDisplayContext.FIXED, poseStack, packedLight, packedOverlay, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
		poseStack.popPose();
	}
}
