/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.entity.model;

import li.cil.oc2.api.API;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public final class RobotModel extends EntityModel<RobotModel.RobotRenderState> {
    public static final ModelLayerLocation ROBOT_MODEL_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(API.MOD_ID, "robot"), "main");
    public static final Identifier ROBOT_ENTITY_TEXTURE = Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/entity/robot/robot.png");

    ///////////////////////////////////////////////////////////////////

    public static final class RobotRenderState extends EntityRenderState {
        public float baseRenderOffsetY;
        public float topRenderOffsetY;
        public float topRenderRotationY;
    }

    ///////////////////////////////////////////////////////////////////

    private final ModelPart topRenderer;
    private final ModelPart baseRenderer;
    private final ModelPart coreRenderer;

    ///////////////////////////////////////////////////////////////////

    public RobotModel(final ModelPart modelPart) {
        super(modelPart);
        topRenderer = modelPart.getChild("top");
        baseRenderer = modelPart.getChild("base");
        coreRenderer = modelPart.getChild("core");
    }

    public static LayerDefinition createRobotLayer() {
        final MeshDefinition meshDefinition = new MeshDefinition();
        final PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(1, 1)
                .addBox(-7, 8, -7, 14, 6, 14),
            PartPose.ZERO);
        partDefinition.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(1, 23)
                .addBox(-7, 0, -7, 14, 7, 14),
            PartPose.ZERO);
        partDefinition.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(1, 34)
                .addBox(-6, 7, -6, 12, 1, 12),
            PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void setupAnim(final RobotRenderState state) {
        super.setupAnim(state);

        topRenderer.y = state.topRenderOffsetY * 16;
        topRenderer.yRot = (float) Math.toRadians(state.topRenderRotationY);

        baseRenderer.y = state.baseRenderOffsetY * 16;
        // TODO(oc2-restore): core was rendered fullbright (LightTexture.pack(15, 15)); Model#renderToBuffer is final in 26.1.
        coreRenderer.y = state.baseRenderOffsetY * 16;
    }
}
