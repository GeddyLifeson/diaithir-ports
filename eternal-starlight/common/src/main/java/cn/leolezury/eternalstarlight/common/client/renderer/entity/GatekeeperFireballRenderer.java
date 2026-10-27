package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESRender;
import cn.leolezury.eternalstarlight.common.entity.projectile.GatekeeperFireball;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import cn.leolezury.eternalstarlight.common.client.compat.ESEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public class GatekeeperFireballRenderer extends ESEntityRenderer<GatekeeperFireball> {

	public GatekeeperFireballRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(GatekeeperFireball entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
		if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.entity().distanceToSqr(entity) < 12.25)) {
			poseStack.pushPose();
			float scale = (Math.min(60f, entity.getSpawnedTicks() + partialTicks) / 60f) * 3f;
			poseStack.scale(scale, scale, scale);
			poseStack.mulPose(ESRender.cameraOrientation());
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			ESRender.submitItem(this.collector, entity.getItem(), ItemDisplayContext.GROUND, poseStack, packedLight, OverlayTexture.NO_OVERLAY, entity.level(), null, entity.getId());
			poseStack.popPose();
		}
	}
}
