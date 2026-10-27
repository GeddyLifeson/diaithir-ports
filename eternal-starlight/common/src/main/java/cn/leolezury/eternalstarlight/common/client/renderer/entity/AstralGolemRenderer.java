package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.AstralGolemModel;
import cn.leolezury.eternalstarlight.common.client.renderer.state.AstralGolemRenderState;
import cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf.golem.AstralGolem;
import cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf.golem.AstralGolemMaterial;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class AstralGolemRenderer extends HumanoidMobRenderer<AstralGolem, AstralGolemRenderState, AstralGolemModel> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/boarwarf/golem/astral_golem_iron.png");

	public AstralGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new AstralGolemModel(context.bakeLayer(AstralGolemModel.LAYER_LOCATION)), 0.5f);
		ArmorModelSet<HumanoidModel<AstralGolemRenderState>> armor = ArmorModelSet.bake(AstralGolemModel.ARMOR_LOCATIONS, context.getModelSet(), HumanoidModel<AstralGolemRenderState>::new);
		// the golem only wears helmets and chestplates
		armor.legs().root().visible = false;
		armor.feet().root().visible = false;
		this.addLayer(new HumanoidArmorLayer<>(this, armor, context.getEquipmentRenderer()));
	}

	@Override
	public AstralGolemRenderState createRenderState() {
		return new AstralGolemRenderState();
	}

	@Override
	public void extractRenderState(AstralGolem entity, AstralGolemRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		AstralGolemMaterial material = entity.getMaterial();
		state.texture = material == null ? ENTITY_TEXTURE : material.textureFull();
		state.tintColor = material == null ? -1 : material.tintColor();
		state.mainHandEmpty = entity.getMainHandItem().isEmpty();
		state.blocking = entity.isGolemBlocking();
		int animationTick = entity.getAttackAnimationTick();
		state.armXRot = animationTick > 0 ? -2.0F + 1.5F * Mth.triangleWave((float) animationTick - partialTicks, 10.0F) : 0;
	}

	@Override
	protected HumanoidModel.ArmPose getArmPose(AstralGolem entity, HumanoidArm arm) {
		return arm == HumanoidArm.LEFT && entity.isGolemBlocking() ? HumanoidModel.ArmPose.BLOCK : HumanoidModel.ArmPose.EMPTY;
	}

	@Override
	protected int getModelTint(AstralGolemRenderState state) {
		return state.tintColor;
	}

	@Override
	public Identifier getTextureLocation(AstralGolemRenderState state) {
		return state.texture;
	}
}
