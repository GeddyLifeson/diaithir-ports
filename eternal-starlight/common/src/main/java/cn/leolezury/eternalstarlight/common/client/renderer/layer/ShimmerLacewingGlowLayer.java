package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderLayer;
import cn.leolezury.eternalstarlight.common.client.model.entity.ShimmerLacewingModel;
import cn.leolezury.eternalstarlight.common.entity.living.animal.ShimmerLacewing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class ShimmerLacewingGlowLayer<T extends ShimmerLacewing> extends ESRenderLayer<T, ShimmerLacewingModel<T>> {
	private final ShimmerLacewingModel<T> model;

	public ShimmerLacewingGlowLayer(RenderLayerParent<ESRenderState, ShimmerLacewingModel<T>> parent, EntityModelSet modelSet) {
		super(parent);
		this.model = new ShimmerLacewingModel<>(modelSet.bakeLayer(ShimmerLacewingModel.LAYER_LOCATION));
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!entity.isInvisible()) {
			getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(entity.getVariant().value().glowTextureFull()));
			this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, ARGB.color((int) (Math.max(0.0F, Mth.cos(ageInTicks * 0.1f + 3.1415927F)) * 255), 255, 255, 255));
		}
	}
}
