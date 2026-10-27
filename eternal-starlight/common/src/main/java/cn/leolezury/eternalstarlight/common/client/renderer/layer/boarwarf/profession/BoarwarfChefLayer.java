package cn.leolezury.eternalstarlight.common.client.renderer.layer.boarwarf.profession;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderLayer;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.boarwarf.BoarwarfModel;
import cn.leolezury.eternalstarlight.common.client.model.entity.boarwarf.profession.BoarwarfChefModel;
import cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf.Boarwarf;
import cn.leolezury.eternalstarlight.common.registry.ESBoarwarfProfessions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;

public class BoarwarfChefLayer<T extends Boarwarf> extends ESRenderLayer<T, BoarwarfModel<T>> {
	private static final Identifier TEXTURE = EternalStarlight.id("textures/entity/boarwarf/profession/chef.png");
	private final BoarwarfChefModel<T> professionModel;

	public BoarwarfChefLayer(RenderLayerParent<ESRenderState, BoarwarfModel<T>> parent, EntityModelSet modelSet) {
		super(parent);
		this.professionModel = new BoarwarfChefModel<>(modelSet.bakeLayer(BoarwarfChefModel.LAYER_LOCATION));
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!entity.isInvisible() && entity.getProfession() == ESBoarwarfProfessions.CHEF.get()) {
			getParentModel().copyPropertiesTo(this.professionModel);
			this.professionModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
			this.professionModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			this.professionModel.copyPropertiesFrom(getParentModel());
			VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.entityCutout(TEXTURE));
			this.professionModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
		}
	}
}
