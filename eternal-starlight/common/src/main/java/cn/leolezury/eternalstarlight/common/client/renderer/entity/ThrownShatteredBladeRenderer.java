package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.client.compat.ESRender;
import cn.leolezury.eternalstarlight.common.client.compat.ESEntityRenderer;
import cn.leolezury.eternalstarlight.common.entity.projectile.ThrownShatteredBlade;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownShatteredBladeRenderer extends ESEntityRenderer<ThrownShatteredBlade> {

	public ThrownShatteredBladeRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(ThrownShatteredBlade entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
		poseStack.pushPose();
		ItemStack itemStack = new ItemStack(ESItems.SHATTERED_SWORD_BLADE.get());
		poseStack.translate(0, entity.getBbHeight() / 2, 0);
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F + (entity.tickCount + partialTicks) * 5));
		ESRender.submitItem(this.collector, itemStack, ItemDisplayContext.GROUND, poseStack, light, OverlayTexture.NO_OVERLAY, entity.level(), entity, entity.getId());
		poseStack.popPose();
		super.render(entity, yaw, partialTicks, poseStack, multiBufferSource, light);
	}

	@Override
	public Identifier getTextureLocation(ThrownShatteredBlade entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
