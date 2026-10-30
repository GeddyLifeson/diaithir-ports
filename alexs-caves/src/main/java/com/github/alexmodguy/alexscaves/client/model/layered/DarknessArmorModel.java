package com.github.alexmodguy.alexscaves.client.model.layered;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class DarknessArmorModel  extends HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> {

    public ModelPart rTail1;
    public ModelPart rTail2;
    public ModelPart lTail1;
    public ModelPart lTail2;
    public ModelPart cape;
    public ModelPart capeTail;
    public DarknessArmorModel(ModelPart root) {
        super(root);
        this.rTail1 = root.getChild("head").getChild("rtail1");
        this.rTail2 = rTail1.getChild("rtail2");
        this.lTail1 = root.getChild("head").getChild("ltail1");
        this.lTail2 = lTail1.getChild("ltail2");
        this.cape = root.getChild("body").getChild("cape");
        this.capeTail = cape.getChild("capeTail");
    }

    public static LayerDefinition createArmorLayer(CubeDeformation deformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition head = partdefinition.getChild("head");
        PartDefinition body = partdefinition.getChild("body");


        head.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(68, 112).addBox(-6.0F, -2.0F, -5.5F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rtail1 = head.addOrReplaceChild("rtail1", CubeListBuilder.create().texOffs(104, 105).addBox(-2.5F, -2.75F, -1.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, -8.25F, 2.0F));
        rtail1.addOrReplaceChild("rtail2", CubeListBuilder.create().texOffs(104, 108).addBox(0.0F, -2.0F, -1.5F, 0.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.25F, 5.5F));

        PartDefinition ltail2 = head.addOrReplaceChild("ltail1", CubeListBuilder.create().texOffs(104, 105).mirror().addBox(-2.5F, -2.75F, -1.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.5F, -8.25F, 2.0F));
        ltail2.addOrReplaceChild("ltail2", CubeListBuilder.create().texOffs(104, 108).mirror().addBox(0.0F, -2.0F, -1.5F, 0.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.25F, 5.5F));

        body.addOrReplaceChild("gem", CubeListBuilder.create().texOffs(57, 123).addBox(-1.5F, 1.0F, -3.5F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cape = body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(30, 111).addBox(-7.0F, 0.5F, 1.0F, 14.0F, 0.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(5, 111).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 3.0F));

        cape.addOrReplaceChild("capeTail", CubeListBuilder.create().texOffs(-17, 94).addBox(-7.0F, 0.0F, -1.5F, 14.0F, 0.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 14.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


}
