package me.desht.pneumaticcraft.client.model.entity.drone;

import me.desht.pneumaticcraft.client.render.entity.drone.DroneRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelDroneCore extends EntityModel<DroneRenderState> {
    private final ModelPart drone;

    private static final String DRONE = "drone";
    private static final String BODY = "body";

    public ModelDroneCore(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        drone = root.getChild(DRONE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition done = partdefinition.addOrReplaceChild(DRONE, CubeListBuilder.create().texOffs(0, 0),
                PartPose.offset(0.0F, 22.5F, 0.0F));
        done.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("body_0", -3.5F, -5.0F, -4.5F, 7, 6, 16, 48, 106),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


}
