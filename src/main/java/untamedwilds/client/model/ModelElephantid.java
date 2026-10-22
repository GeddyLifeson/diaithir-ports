package untamedwilds.client.model;

import com.google.common.collect.ImmutableList;
import untamedwilds.citadel.client.model.AdvancedEntityModel;
import untamedwilds.citadel.client.model.AdvancedModelBox;
import untamedwilds.citadel.client.model.ModelAnimator;
import untamedwilds.citadel.client.model.basic.BasicModelPart;
import net.minecraft.util.Mth;
import untamedwilds.entity.mammal.EntityElephantid;

/** Code-built model generated for the elephantid body plan (see UW_SPECIES.csv for the species it serves). */
public class ModelElephantid extends AdvancedEntityModel<EntityElephantid> {

    private final AdvancedModelBox body_main;
    private final AdvancedModelBox arm_left_1;
    private final AdvancedModelBox arm_left_2;
    private final AdvancedModelBox arm_left_3;
    private final AdvancedModelBox arm_right_1;
    private final AdvancedModelBox arm_right_2;
    private final AdvancedModelBox arm_right_3;
    private final AdvancedModelBox leg_left_1;
    private final AdvancedModelBox leg_left_2;
    private final AdvancedModelBox leg_left_3;
    private final AdvancedModelBox leg_right_1;
    private final AdvancedModelBox leg_right_2;
    private final AdvancedModelBox leg_right_3;
    private final AdvancedModelBox head_neck;
    private final AdvancedModelBox head_main;
    private final AdvancedModelBox eye_left;
    private final AdvancedModelBox eye_right;
    private final AdvancedModelBox head_ear_left;
    private final AdvancedModelBox head_ear_right;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox trunk_1;
    private final AdvancedModelBox trunk_2;
    private final AdvancedModelBox trunk_3;
    private final AdvancedModelBox body_hump;
    private final AdvancedModelBox head_dome;
    private final AdvancedModelBox head_hair;
    private final AdvancedModelBox skirt_left;
    private final AdvancedModelBox skirt_right;
    private final AdvancedModelBox ear_big_left;
    private final AdvancedModelBox ear_big_right;
    private final AdvancedModelBox tusk_1_left;
    private final AdvancedModelBox tusk_1_right;
    private final AdvancedModelBox tusk_2_left;
    private final AdvancedModelBox tusk_2_right;
    private final AdvancedModelBox tusk_3_left;
    private final AdvancedModelBox tusk_3_right;
    private static final int[] SET_0 = {0, 1, 2, 3, 4, 6};
    private static final int[] SET_1 = {0, 1, 4};
    private static final int[] SET_2 = {0, 2};
    private static final int[] SET_3 = {5};
    private static final int[] SET_4 = {0, 1, 2, 3, 4, 5};

    public ModelElephantid() {
        this.texWidth = 256;
        this.texHeight = 128;

        this.body_main = new AdvancedModelBox(this, 0, 0);
        this.body_main.setRotationPoint(0F, -10F, 0F);
        this.body_main.addBox(-11F, -13F, -17F, 22F, 26F, 34F, 0F);
        this.arm_left_1 = new AdvancedModelBox(this, 0, 60);
        this.arm_left_1.setRotationPoint(6F, 11F, -11.5F);
        this.arm_left_1.addBox(-4.5F, 0F, -4.5F, 9F, 12F, 9F, 0F);
        this.arm_left_2 = new AdvancedModelBox(this, 176, 60);
        this.arm_left_2.setRotationPoint(0F, 11F, 0F);
        this.arm_left_2.addBox(-4F, 0F, -4F, 8F, 10F, 8F, 0F);
        this.arm_left_3 = new AdvancedModelBox(this, 196, 81);
        this.arm_left_3.setRotationPoint(0F, 9F, 0F);
        this.arm_left_3.addBox(-4.5F, 0F, -4.5F, 9F, 3F, 9F, 0F);
        this.arm_right_1 = new AdvancedModelBox(this, 0, 60);
        this.arm_right_1.mirror = true;
        this.arm_right_1.setRotationPoint(-6F, 11F, -11.5F);
        this.arm_right_1.addBox(-4.5F, 0F, -4.5F, 9F, 12F, 9F, 0F);
        this.arm_right_2 = new AdvancedModelBox(this, 176, 60);
        this.arm_right_2.mirror = true;
        this.arm_right_2.setRotationPoint(0F, 11F, 0F);
        this.arm_right_2.addBox(-4F, 0F, -4F, 8F, 10F, 8F, 0F);
        this.arm_right_3 = new AdvancedModelBox(this, 196, 81);
        this.arm_right_3.mirror = true;
        this.arm_right_3.setRotationPoint(0F, 9F, 0F);
        this.arm_right_3.addBox(-4.5F, 0F, -4.5F, 9F, 3F, 9F, 0F);
        this.leg_left_1 = new AdvancedModelBox(this, 36, 60);
        this.leg_left_1.setRotationPoint(6F, 11F, 11.5F);
        this.leg_left_1.addBox(-4.5F, 0F, -4.5F, 9F, 12F, 9F, 0F);
        this.leg_left_2 = new AdvancedModelBox(this, 208, 60);
        this.leg_left_2.setRotationPoint(0F, 11F, 0F);
        this.leg_left_2.addBox(-4F, 0F, -4F, 8F, 10F, 8F, 0F);
        this.leg_left_3 = new AdvancedModelBox(this, 0, 97);
        this.leg_left_3.setRotationPoint(0F, 9F, 0F);
        this.leg_left_3.addBox(-4.5F, 0F, -4.5F, 9F, 3F, 9F, 0F);
        this.leg_right_1 = new AdvancedModelBox(this, 36, 60);
        this.leg_right_1.mirror = true;
        this.leg_right_1.setRotationPoint(-6F, 11F, 11.5F);
        this.leg_right_1.addBox(-4.5F, 0F, -4.5F, 9F, 12F, 9F, 0F);
        this.leg_right_2 = new AdvancedModelBox(this, 208, 60);
        this.leg_right_2.mirror = true;
        this.leg_right_2.setRotationPoint(0F, 11F, 0F);
        this.leg_right_2.addBox(-4F, 0F, -4F, 8F, 10F, 8F, 0F);
        this.leg_right_3 = new AdvancedModelBox(this, 0, 97);
        this.leg_right_3.mirror = true;
        this.leg_right_3.setRotationPoint(0F, 9F, 0F);
        this.leg_right_3.addBox(-4.5F, 0F, -4.5F, 9F, 3F, 9F, 0F);
        this.head_neck = new AdvancedModelBox(this, 92, 60);
        this.head_neck.setRotationPoint(0F, -3F, -16F);
        this.head_neck.addBox(-6F, -7F, -6F, 12F, 14F, 6F, 0F);
        this.setRotateAngle(head_neck, -0.2F, 0F, 0F);
        this.head_main = new AdvancedModelBox(this, 194, 0);
        this.head_main.setRotationPoint(0F, 0F, -5F);
        this.head_main.addBox(-7F, -8F, -12F, 14F, 16F, 13F, 0F);
        this.setRotateAngle(head_main, 0.3F, 0F, 0F);
        this.eye_left = new AdvancedModelBox(this, 194, 29);
        this.eye_left.mirror = true;
        this.eye_left.setRotationPoint(7.02F, -4F, -10F);
        this.eye_left.addBox(0F, -2F, -2.5F, 0F, 4F, 5F, 0F);
        this.eye_right = new AdvancedModelBox(this, 194, 29);
        this.eye_right.setRotationPoint(-7.02F, -4F, -10F);
        this.eye_right.addBox(0F, -2F, -2.5F, 0F, 4F, 5F, 0F);
        this.head_ear_left = new AdvancedModelBox(this, 72, 60);
        this.head_ear_left.setRotationPoint(6.5F, -6.5F, 1.5F);
        this.head_ear_left.addBox(0F, -6F, 0F, 1F, 12F, 9F, 0F);
        this.setRotateAngle(head_ear_left, 0.1F, 0.5F, 0F);
        this.head_ear_right = new AdvancedModelBox(this, 72, 60);
        this.head_ear_right.mirror = true;
        this.head_ear_right.setRotationPoint(-6.5F, -6.5F, 1.5F);
        this.head_ear_right.addBox(-1F, -6F, 0F, 1F, 12F, 9F, 0F);
        this.setRotateAngle(head_ear_right, 0.1F, -0.5F, 0F);
        this.tail = new AdvancedModelBox(this, 72, 81);
        this.tail.setRotationPoint(0F, -11F, 16.5F);
        this.tail.addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, 0F);
        this.setRotateAngle(tail, 0.1F, 0F, 0F);
        this.trunk_1 = new AdvancedModelBox(this, 0, 81);
        this.trunk_1.setRotationPoint(0F, 3F, -12F);
        this.trunk_1.addBox(-3.5F, 0F, -3.5F, 7F, 9F, 7F, 0F);
        this.setRotateAngle(trunk_1, 0.15F, 0F, 0F);
        this.trunk_2 = new AdvancedModelBox(this, 84, 81);
        this.trunk_2.setRotationPoint(0F, 8F, 0F);
        this.trunk_2.addBox(-3F, 0F, -3F, 6F, 9F, 6F, 0F);
        this.setRotateAngle(trunk_2, 0.1F, 0F, 0F);
        this.trunk_3 = new AdvancedModelBox(this, 176, 81);
        this.trunk_3.setRotationPoint(0F, 8F, 0F);
        this.trunk_3.addBox(-2.5F, 0F, -2.5F, 5F, 8F, 5F, 0F);
        this.setRotateAngle(trunk_3, -0.3F, 0F, 0F);
        this.body_hump = new AdvancedModelBox(this, 128, 60);
        this.body_hump.setRotationPoint(0F, -12F, -8F);
        this.body_hump.addBox(-6F, -6F, -6F, 12F, 7F, 12F, 0F);
        this.head_dome = new AdvancedModelBox(this, 28, 81);
        this.head_dome.setRotationPoint(0F, -7F, -3F);
        this.head_dome.addBox(-6F, -4F, -5F, 12F, 6F, 10F, 0F);
        this.head_hair = new AdvancedModelBox(this, 108, 81);
        this.head_hair.setRotationPoint(0F, -8.5F, -2F);
        this.head_hair.addBox(-5F, -3F, -5F, 10F, 4F, 10F, 0F);
        this.skirt_left = new AdvancedModelBox(this, 112, 0);
        this.skirt_left.setRotationPoint(11.3F, 7F, 0F);
        this.skirt_left.addBox(0F, 0F, -14F, 0F, 12F, 28F, 0F);
        this.skirt_right = new AdvancedModelBox(this, 112, 0);
        this.skirt_right.mirror = true;
        this.skirt_right.setRotationPoint(-11.3F, 7F, 0F);
        this.skirt_right.addBox(0F, 0F, -14F, 0F, 12F, 28F, 0F);
        this.ear_big_left = new AdvancedModelBox(this, 168, 0);
        this.ear_big_left.setRotationPoint(6.5F, -6.5F, 1.5F);
        this.ear_big_left.addBox(0F, -9F, 0F, 1F, 18F, 12F, 0F);
        this.setRotateAngle(ear_big_left, 0.1F, 0.5F, 0F);
        this.ear_big_right = new AdvancedModelBox(this, 168, 0);
        this.ear_big_right.mirror = true;
        this.ear_big_right.setRotationPoint(-6.5F, -6.5F, 1.5F);
        this.ear_big_right.addBox(-1F, -9F, 0F, 1F, 18F, 12F, 0F);
        this.setRotateAngle(ear_big_right, 0.1F, -0.5F, 0F);
        this.tusk_1_left = new AdvancedModelBox(this, 148, 81);
        this.tusk_1_left.setRotationPoint(5F, 4F, -10F);
        this.tusk_1_left.addBox(-2F, -2F, -10F, 4F, 4F, 10F, 0F);
        this.setRotateAngle(tusk_1_left, 0.45F, -0.15F, 0F);
        this.tusk_1_right = new AdvancedModelBox(this, 148, 81);
        this.tusk_1_right.mirror = true;
        this.tusk_1_right.setRotationPoint(-5F, 4F, -10F);
        this.tusk_1_right.addBox(-2F, -2F, -10F, 4F, 4F, 10F, 0F);
        this.setRotateAngle(tusk_1_right, 0.45F, 0.15F, 0F);
        this.tusk_2_left = new AdvancedModelBox(this, 36, 97);
        this.tusk_2_left.setRotationPoint(0F, 0F, -10F);
        this.tusk_2_left.addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, 0F);
        this.setRotateAngle(tusk_2_left, -0.7F, 0F, 0F);
        this.tusk_2_right = new AdvancedModelBox(this, 36, 97);
        this.tusk_2_right.mirror = true;
        this.tusk_2_right.setRotationPoint(0F, 0F, -10F);
        this.tusk_2_right.addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, 0F);
        this.setRotateAngle(tusk_2_right, -0.7F, 0F, 0F);
        this.tusk_3_left = new AdvancedModelBox(this, 58, 97);
        this.tusk_3_left.setRotationPoint(0F, 0F, -8F);
        this.tusk_3_left.addBox(-1F, -1F, -5F, 2F, 2F, 5F, 0F);
        this.setRotateAngle(tusk_3_left, -0.6F, 0F, 0F);
        this.tusk_3_right = new AdvancedModelBox(this, 58, 97);
        this.tusk_3_right.mirror = true;
        this.tusk_3_right.setRotationPoint(0F, 0F, -8F);
        this.tusk_3_right.addBox(-1F, -1F, -5F, 2F, 2F, 5F, 0F);
        this.setRotateAngle(tusk_3_right, -0.6F, 0F, 0F);
        this.body_main.addChild(this.arm_left_1);
        this.arm_left_1.addChild(this.arm_left_2);
        this.arm_left_2.addChild(this.arm_left_3);
        this.body_main.addChild(this.arm_right_1);
        this.arm_right_1.addChild(this.arm_right_2);
        this.arm_right_2.addChild(this.arm_right_3);
        this.body_main.addChild(this.leg_left_1);
        this.leg_left_1.addChild(this.leg_left_2);
        this.leg_left_2.addChild(this.leg_left_3);
        this.body_main.addChild(this.leg_right_1);
        this.leg_right_1.addChild(this.leg_right_2);
        this.leg_right_2.addChild(this.leg_right_3);
        this.body_main.addChild(this.head_neck);
        this.head_neck.addChild(this.head_main);
        this.head_main.addChild(this.eye_left);
        this.head_main.addChild(this.eye_right);
        this.head_main.addChild(this.head_ear_left);
        this.head_main.addChild(this.head_ear_right);
        this.body_main.addChild(this.tail);
        this.head_main.addChild(this.trunk_1);
        this.trunk_1.addChild(this.trunk_2);
        this.trunk_2.addChild(this.trunk_3);
        this.body_main.addChild(this.body_hump);
        this.head_main.addChild(this.head_dome);
        this.head_main.addChild(this.head_hair);
        this.body_main.addChild(this.skirt_left);
        this.body_main.addChild(this.skirt_right);
        this.head_main.addChild(this.ear_big_left);
        this.head_main.addChild(this.ear_big_right);
        this.head_main.addChild(this.tusk_1_left);
        this.head_main.addChild(this.tusk_1_right);
        this.tusk_1_left.addChild(this.tusk_2_left);
        this.tusk_1_right.addChild(this.tusk_2_right);
        this.tusk_2_left.addChild(this.tusk_3_left);
        this.tusk_2_right.addChild(this.tusk_3_right);
        updateDefaultPose();
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(body_main);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(
            body_main,
            arm_left_1,
            arm_left_2,
            arm_left_3,
            arm_right_1,
            arm_right_2,
            arm_right_3,
            leg_left_1,
            leg_left_2,
            leg_left_3,
            leg_right_1,
            leg_right_2,
            leg_right_3,
            head_neck,
            head_main,
            eye_left,
            eye_right,
            head_ear_left,
            head_ear_right,
            tail,
            trunk_1,
            trunk_2,
            trunk_3,
            body_hump,
            head_dome,
            head_hair,
            skirt_left,
            skirt_right,
            ear_big_left,
            ear_big_right,
            tusk_1_left,
            tusk_1_right,
            tusk_2_left,
            tusk_2_right,
            tusk_3_left,
            tusk_3_right
        );
    }

    private static boolean in(int[] set, int v) {
        for (int s : set) {
            if (s == v) return true;
        }
        return false;
    }

    public void setupAnim(EntityElephantid mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        this.head_ear_left.showModel = in(SET_0, mob.getVariant());
        this.head_ear_right.showModel = in(SET_0, mob.getVariant());
        this.body_hump.showModel = in(SET_1, mob.getVariant());
        this.head_dome.showModel = in(SET_1, mob.getVariant());
        this.head_hair.showModel = in(SET_2, mob.getVariant());
        this.skirt_left.showModel = in(SET_2, mob.getVariant());
        this.skirt_right.showModel = in(SET_2, mob.getVariant());
        this.ear_big_left.showModel = in(SET_3, mob.getVariant());
        this.ear_big_right.showModel = in(SET_3, mob.getVariant());
        this.tusk_1_left.showModel = in(SET_4, mob.getVariant());
        this.tusk_1_right.showModel = in(SET_4, mob.getVariant());
        this.tusk_2_left.showModel = in(SET_4, mob.getVariant());
        this.tusk_2_right.showModel = in(SET_4, mob.getVariant());
        this.tusk_3_left.showModel = in(SET_4, mob.getVariant());
        this.tusk_3_right.showModel = in(SET_4, mob.getVariant());
        float globalSpeed = 1.5f;
        float globalDegree = 1f;
        float f = limbSwing / 2;
        limbSwingAmount = Math.min(0.4F, limbSwingAmount);
        float gait = 0.7F;
        // Breathing
        this.body_main.setScale((float) (1.0F + Math.sin(ageInTicks / 20) * 0.03F), (float) (1.0F + Math.sin(ageInTicks / 16) * 0.03F), 1.0F);
        walk(head_neck, 0.4f * globalSpeed, 0.03f, false, 2.8F, 0.0F, ageInTicks / 20, 2);
        walk(trunk_1, 0.35F, 0.12F, false, 0F, 0.0F, ageInTicks / 20, 2);
        walk(trunk_2, 0.35F, 0.12F, false, 0.9F, 0.0F, ageInTicks / 20, 2);
        walk(trunk_3, 0.35F, 0.12F, false, 1.8F, 0.0F, ageInTicks / 20, 2);
        swing(head_ear_left, 0.5F, 0.08F, false, 0F, 0F, ageInTicks / 20, 2);
        swing(head_ear_right, 0.5F, 0.08F, false, 0F, 0F, ageInTicks / 20, 2);
        swing(tail, 0.4F, 0.15F, false, 0F, 0F, ageInTicks / 20, 2);
        if (!mob.shouldRenderEyes()) {
            this.eye_right.showModel = false;
            this.eye_left.showModel = false;
        }
        if (!mob.isSleeping()) {
            this.faceTarget(netHeadYaw, headPitch, 3, head_neck);
            this.faceTarget(netHeadYaw, headPitch, 3, head_main);
        }
        if (mob.canMove()) {
            bob(body_main, 0.8f * globalSpeed * gait, 0.4f * globalDegree, true, f, limbSwingAmount);
            walk(head_neck, 0.8f * globalSpeed * gait, 0.12f * globalDegree, false, 0, 0, f, limbSwingAmount);
            swing(tail, 0.8f * globalSpeed * gait, 0.3f, false, 1F, 0F, f, limbSwingAmount);
            walk(arm_left_1, 0.8f * globalSpeed * gait, 1.0f * globalDegree, false, 0F, 0, f, limbSwingAmount);
            walk(arm_left_2, 0.8f * globalSpeed * gait, 0.7f * globalDegree, true, 0.9F, 0.3F, f, limbSwingAmount);
            walk(arm_right_1, 0.8f * globalSpeed * gait, 1.0f * globalDegree, false, 3.14F, 0, f, limbSwingAmount);
            walk(arm_right_2, 0.8f * globalSpeed * gait, 0.7f * globalDegree, true, 4.04F, 0.3F, f, limbSwingAmount);
            walk(leg_left_1, 0.8f * globalSpeed * gait, 1.0f * globalDegree, false, 3.14F, 0, f, limbSwingAmount);
            walk(leg_left_2, 0.8f * globalSpeed * gait, 0.7f * globalDegree, true, 4.04F, 0.3F, f, limbSwingAmount);
            walk(leg_right_1, 0.8f * globalSpeed * gait, 1.0f * globalDegree, false, 0F, 0, f, limbSwingAmount);
            walk(leg_right_2, 0.8f * globalSpeed * gait, 0.7f * globalDegree, true, 0.9F, 0.3F, f, limbSwingAmount);
        }
        float rest = Math.max(mob.sitProgress, mob.sleepProgress);
        if (rest > 0) {
            this.progressPosition(body_main, rest, 0.0F, 15F, 0.0F, 40);
            this.progressRotation(head_neck, rest, 0.6F, 0, 0, 40);
            this.progressRotation(arm_left_1, rest, -1.25F, 0.25F, 0, 40);
            this.progressRotation(arm_left_2, rest, -0.2F, 0, 0, 40);
            this.progressRotation(arm_right_1, rest, -1.25F, -0.25F, 0, 40);
            this.progressRotation(arm_right_2, rest, -0.2F, 0, 0, 40);
            this.progressRotation(leg_left_1, rest, -1.25F, 0.25F, 0, 40);
            this.progressRotation(leg_left_2, rest, -0.2F, 0, 0, 40);
            this.progressRotation(leg_right_1, rest, -1.25F, -0.25F, 0, 40);
            this.progressRotation(leg_right_2, rest, -0.2F, 0, 0, 40);
        }
    }
}
