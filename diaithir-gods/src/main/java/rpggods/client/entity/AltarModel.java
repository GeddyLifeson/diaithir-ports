package rpggods.client.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;
import rpggods.util.altar.AltarPose;
import rpggods.util.altar.HumanoidPart;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * The statue body. It has an extra set of slim arms and a chest piece that are shown or hidden
 * depending on the altar, and every part is rotated by the altar pose.
 */
public class AltarModel extends AltarArmorModel {

    protected final ModelPart bodyChest;
    protected final ModelPart rightArmSlim;
    protected final ModelPart leftArmSlim;
    protected final ModelPart leftSleeveSlim;
    protected final ModelPart rightSleeveSlim;
    protected final ModelPart leftSleeve;
    protected final ModelPart rightSleeve;
    protected final ModelPart leftPants;
    protected final ModelPart rightPants;
    protected final ModelPart jacket;
    private final EnumMap<HumanoidPart, Collection<ModelPart>> rotationMap = new EnumMap<>(HumanoidPart.class);
    private boolean slim;

    public AltarModel(final ModelPart root) {
        super(root);
        this.bodyChest = root.getChild("chest");
        this.leftArmSlim = root.getChild("left_arm_slim");
        this.rightArmSlim = root.getChild("right_arm_slim");
        this.leftSleeve = root.getChild("left_sleeve");
        this.rightSleeve = root.getChild("right_sleeve");
        this.leftSleeveSlim = root.getChild("left_sleeve_slim");
        this.rightSleeveSlim = root.getChild("right_sleeve_slim");
        this.leftPants = root.getChild("left_pants");
        this.rightPants = root.getChild("right_pants");
        this.jacket = root.getChild("jacket");

        rotationMap.put(HumanoidPart.HEAD, ImmutableList.of(this.head));
        rotationMap.put(HumanoidPart.BODY, ImmutableList.of(this.body, this.bodyChest, this.jacket));
        rotationMap.put(HumanoidPart.LEFT_ARM, ImmutableList.of(this.leftArm, this.leftArmSlim, this.leftSleeve, this.leftSleeveSlim));
        rotationMap.put(HumanoidPart.RIGHT_ARM, ImmutableList.of(this.rightArm, this.rightArmSlim, this.rightSleeve, this.rightSleeveSlim));
        rotationMap.put(HumanoidPart.LEFT_LEG, ImmutableList.of(this.leftLeg, this.leftPants));
        rotationMap.put(HumanoidPart.RIGHT_LEG, ImmutableList.of(this.rightLeg, this.rightPants));
    }

    public static LayerDefinition createBodyLayer() {
        CubeDeformation none = CubeDeformation.NONE;
        MeshDefinition mesh = HumanoidModel.createMesh(none, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, none), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, none.extend(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, none.extend(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, none.extend(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, none.extend(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, none.extend(0.25F)), PartPose.ZERO);
        root.addOrReplaceChild("left_arm_slim", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, none), PartPose.offset(5.0F, 2.5F, 0.0F));
        root.addOrReplaceChild("right_arm_slim", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, none), PartPose.offset(-5.0F, 2.5F, 0.0F));
        root.addOrReplaceChild("left_sleeve_slim", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, none.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
        root.addOrReplaceChild("right_sleeve_slim", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, none.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
        root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(19, 20).addBox(-4.01F, 0.0F, 0.0F, 8.0F, 4.0F, 1.0F, none), PartPose.offsetAndRotation(0.0F, 1.0F, -2.0F, -0.2182F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AltarRenderState state) {
        this.resetPose();
        this.slim = state.slim;
        final AltarPose pose = state.pose;
        for (final Map.Entry<HumanoidPart, Collection<ModelPart>> e : rotationMap.entrySet()) {
            final Vector3f rotations = pose.get(e.getKey());
            for (final ModelPart m : e.getValue()) {
                m.xRot = rotations.x();
                m.yRot = rotations.y();
                m.zRot = rotations.z();
            }
        }
        // reset body rotations
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;
        this.jacket.xRot = 0.0F;
        this.jacket.yRot = 0.0F;
        this.jacket.zRot = 0.0F;
        this.bodyChest.xRot = -0.2182F;
        this.bodyChest.yRot = 0.0F;
        this.bodyChest.zRot = 0.0F;
        // choose which parts are shown
        this.bodyChest.visible = state.female;
        this.leftArm.visible = !state.slim;
        this.rightArm.visible = !state.slim;
        this.leftSleeve.visible = !state.slim;
        this.rightSleeve.visible = !state.slim;
        this.leftArmSlim.visible = state.slim;
        this.rightArmSlim.visible = state.slim;
        this.leftSleeveSlim.visible = state.slim;
        this.rightSleeveSlim.visible = state.slim;
    }

    @Override
    public ModelPart getArm(HumanoidArm arm) {
        if (slim) {
            return arm == HumanoidArm.LEFT ? this.leftArmSlim : this.rightArmSlim;
        }
        return super.getArm(arm);
    }
}
