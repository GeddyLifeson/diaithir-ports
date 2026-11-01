package com.github.ysbbbbbb.kaleidoscopetavern.client.model.brew;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.client.render.block.state.BarrelRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class BarrelModel extends Model<BarrelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(KaleidoscopeTavern.modLoc("barrel"), "main");

    private final ModelPart close;
    private final ModelPart open;

    public BarrelModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        ModelPart base = this.root().getChild("base");
        this.close = base.getChild("close");
        this.open = base.getChild("open");
    }

    @Override
    public void setupAnim(BarrelRenderState state) {
        // 根据桶的开盖状态切换模型显示
        this.close.visible = !state.isOpen;
        this.open.visible = state.isOpen;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(8.0F, 9.0F, -9.0F));

        PartDefinition close = base.addOrReplaceChild("close", CubeListBuilder.create().texOffs(102, 113).addBox(-16.0F, -33.0F, 1.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition open = base.addOrReplaceChild("open", CubeListBuilder.create(), PartPose.offsetAndRotation(-8.0F, -33.0F, -1.0F, 1.309F, 0.0F, 0.0F));

        PartDefinition open_r1 = open.addOrReplaceChild("open_r1", CubeListBuilder.create().texOffs(106, 114).mirror().addBox(0.0F, -4.0F, -12.0F, 0.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.0F, 9.7654F, 5.0978F, -0.6109F, 0.0F, 0.0F));

        PartDefinition open_r2 = open.addOrReplaceChild("open_r2", CubeListBuilder.create().texOffs(102, 113).addBox(-8.0F, -2.4F, -2.8F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 8.0F, 0.5672F, 0.0F, 0.0F));

        PartDefinition body = base.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 136).addBox(6.0F, 7.0F, -10.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(28, 136).addBox(-26.0F, 7.0F, -10.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(174, 118).addBox(-22.0F, 7.0F, -8.0F, 28.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(174, 118).addBox(-22.0F, 7.0F, 22.0F, 28.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 136).addBox(6.0F, 7.0F, 22.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(28, 136).addBox(-26.0F, 7.0F, 22.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-28.0F, -33.0F, -15.0F, 40.0F, 40.0F, 48.0F, new CubeDeformation(0.0F))
                .texOffs(0, 160).addBox(-16.0F, -33.0F, 1.0F, 16.0F, 21.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 144).addBox(0.0F, -33.0F, 1.0F, 0.0F, 24.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 144).addBox(-16.0F, -33.0F, 1.0F, 0.0F, 21.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(32, 160).addBox(-16.0F, -12.0F, 1.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 160).addBox(-16.0F, -33.0F, 17.0F, 16.0F, 21.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }
}
