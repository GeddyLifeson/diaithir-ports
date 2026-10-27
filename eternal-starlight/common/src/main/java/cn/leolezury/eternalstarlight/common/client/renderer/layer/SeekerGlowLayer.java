package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderLayer;
import cn.leolezury.eternalstarlight.common.client.model.entity.SeekerModel;
import cn.leolezury.eternalstarlight.common.entity.living.monster.Seeker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SeekerGlowLayer<T extends Seeker, M extends SeekerModel<T>> extends ESRenderLayer<T, M> {
	public SeekerGlowLayer(RenderLayerParent<ESRenderState, M> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		VertexConsumer consumer = buffer.getBuffer(RenderTypes.eyes(entity.getVariant().value().glowTextureFull()));
		this.getParentModel().renderToBuffer(poseStack, consumer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
	}
}
