package rpggods.client.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import org.joml.Vector3f;
import rpggods.util.altar.AltarPose;
import rpggods.util.altar.HumanoidPart;

/** Armor model that follows the pose of the altar instead of walking animations **/
public class AltarArmorModel extends HumanoidModel<AltarRenderState> {

    public AltarArmorModel(ModelPart root) {
        super(root);
    }

    public static ArmorModelSet<LayerDefinition> createArmorLayerSet(CubeDeformation inner, CubeDeformation outer) {
        return HumanoidModel.createArmorMeshSet(inner, outer).map(mesh -> LayerDefinition.create(mesh, 64, 32));
    }

    /** Applies the rotation of the given part **/
    protected static void rotate(final ModelPart part, final AltarPose pose, final HumanoidPart type) {
        final Vector3f rot = pose.get(type);
        part.xRot = rot.x();
        part.yRot = rot.y();
        part.zRot = rot.z();
    }

    @Override
    public void setupAnim(AltarRenderState state) {
        this.resetPose();
        final AltarPose pose = state.pose;
        rotate(this.head, pose, HumanoidPart.HEAD);
        this.head.setPos(0.0F, 1.0F, 0.0F);
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;
        rotate(this.leftArm, pose, HumanoidPart.LEFT_ARM);
        rotate(this.rightArm, pose, HumanoidPart.RIGHT_ARM);
        rotate(this.leftLeg, pose, HumanoidPart.LEFT_LEG);
        this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
        rotate(this.rightLeg, pose, HumanoidPart.RIGHT_LEG);
        this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
    }
}
