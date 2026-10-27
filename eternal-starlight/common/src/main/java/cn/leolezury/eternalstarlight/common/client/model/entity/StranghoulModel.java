package cn.leolezury.eternalstarlight.common.client.model.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.ESArmorSets;
import cn.leolezury.eternalstarlight.common.client.renderer.state.StranghoulRenderState;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class StranghoulModel extends HumanoidModel<StranghoulRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(EternalStarlight.id("stranghoul"), "main");
	public static final ArmorModelSet<ModelLayerLocation> ARMOR_LOCATIONS = ESArmorSets.create("stranghoul");

	public StranghoulModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -0.5F));

		head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -32.0F, -4.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 24.0F, 0.5F));

		partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(12, 28).addBox(-3.0F, 8.0F, -1.5F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
			.texOffs(12, 16).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(36, 20).mirror().addBox(-2.0F, -2.0F, -2.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.5F));

		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 20).addBox(-1.0F, -2.0F, -2.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.5F));

		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-1.3F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.6F, 12.0F, 0.0F));

		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 20).addBox(-1.7F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.6F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(StranghoulRenderState state) {
		super.setupAnim(state);
		if (state.isAggressive && state.weaponEmpty) {
			AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, state);
		}
		if (state.eating) {
			boolean inverted = state.mainArm == HumanoidArm.RIGHT;
			ModelPart eatingArm = getArm(state.mainArm.getOpposite());
			eatingArm.xRot = (Mth.sin(state.ageInTicks * 1.5f) + 1) * 0.5f * Mth.PI * 0.12f - Mth.PI * 0.5f;
			eatingArm.yRot = inverted ? 0.4f : -0.4f;
			eatingArm.zRot = 0;
		}
		if (state.bartering) {
			this.head.xRot = 0.5F;
			this.head.yRot = 0.0F;
			if (state.mainArm == HumanoidArm.LEFT) {
				this.rightArm.yRot = -0.5F;
				this.rightArm.xRot = -0.9F;
			} else {
				this.leftArm.yRot = 0.5F;
				this.leftArm.xRot = -0.9F;
			}
		}
	}
}
