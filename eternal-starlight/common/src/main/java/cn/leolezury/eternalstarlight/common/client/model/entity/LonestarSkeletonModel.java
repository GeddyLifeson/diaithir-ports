package cn.leolezury.eternalstarlight.common.client.model.entity;

import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class LonestarSkeletonModel extends SkeletonModel<SkeletonRenderState> {
	private final ModelPart innerHead;
	private final RandomSource random = RandomSource.create();

	public LonestarSkeletonModel(ModelPart root) {
		super(root);
		if (this.head.hasChild("inner_head")) {
			this.innerHead = this.head.getChild("inner_head");
		} else {
			this.innerHead = null;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		createDefaultSkeletonMesh(partDefinition);
		partDefinition.getChild("head").addOrReplaceChild("inner_head", CubeListBuilder.create().texOffs(0, 32).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));
		partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(48, 24).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public void setupAnim(SkeletonRenderState state) {
		super.setupAnim(state);
		if (innerHead != null) {
			innerHead.resetPose();
			if (!Minecraft.getInstance().isPaused()) {
				innerHead.x += (float) (random.nextGaussian() * 0.25);
				innerHead.y += (float) (random.nextGaussian() * 0.25);
			}
		}
	}
}
