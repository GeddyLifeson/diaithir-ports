package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderLayer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.TheGatekeeperModel;
import cn.leolezury.eternalstarlight.common.client.renderer.entity.TheGatekeeperRenderer;
import cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper.TheGatekeeper;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class TheGatekeeperClothingLayer<T extends TheGatekeeper> extends ESRenderLayer<T, TheGatekeeperModel<T>> {
	private static final Identifier OVERLAY_TEXTURE = EternalStarlight.id("textures/entity/the_gatekeeper_overlay.png");
	private static final Identifier SLIM_OVERLAY_TEXTURE = EternalStarlight.id("textures/entity/the_gatekeeper_overlay_slim.png");
	private final TheGatekeeperModel<T> normalModel;
	private final TheGatekeeperModel<T> slimModel;

	public TheGatekeeperClothingLayer(RenderLayerParent<ESRenderState, TheGatekeeperModel<T>> parent, EntityModelSet modelSet) {
		super(parent);
		this.normalModel = new TheGatekeeperModel<>(modelSet.bakeLayer(TheGatekeeperModel.OUTER_LAYER_LOCATION), false);
		this.slimModel = new TheGatekeeperModel<>(modelSet.bakeLayer(TheGatekeeperModel.SLIM_OUTER_LAYER_LOCATION), true);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		TheGatekeeperModel<T> model = normalModel;
		Identifier texture = OVERLAY_TEXTURE;
		Optional<GameProfile> profile = TheGatekeeperRenderer.getGameProfile(entity);
		if (profile.isPresent()) {
			boolean slim = TheGatekeeperRenderer.getSkin(profile.get()).map(PlayerSkin::model).orElse(PlayerModelType.WIDE) == PlayerModelType.SLIM;
			texture = slim ? SLIM_OVERLAY_TEXTURE : OVERLAY_TEXTURE;
			model = slim ? slimModel : normalModel;
		}
		if (!entity.isInvisible()) {
			model.alphaFactor = getParentModel().alphaFactor;
			getParentModel().copyPropertiesTo(model);
			model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.entityTranslucent(texture));
			model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
		}
	}
}
