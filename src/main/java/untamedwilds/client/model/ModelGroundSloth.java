package untamedwilds.client.model;

import com.google.common.collect.ImmutableList;
import untamedwilds.citadel.client.model.AdvancedEntityModel;
import untamedwilds.citadel.client.model.AdvancedModelBox;
import untamedwilds.citadel.client.model.ModelAnimator;
import untamedwilds.citadel.client.model.basic.BasicModelPart;
import net.minecraft.util.Mth;
import untamedwilds.entity.mammal.EntityGroundSloth;

/** Code-built model generated for the ground_sloth body plan (see UW_SPECIES.csv for the species it serves). */
public class ModelGroundSloth extends AdvancedEntityModel<EntityGroundSloth> {

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
    private final AdvancedModelBox head_snout;
    private final AdvancedModelBox eye_left;
    private final AdvancedModelBox eye_right;
    private final AdvancedModelBox head_ear_left;
    private final AdvancedModelBox head_ear_right;
    private final AdvancedModelBox tail;
    private final AdvancedModelBox skirt_left;
    private final AdvancedModelBox skirt_right;
    private final AdvancedModelBox mane;
    private final AdvancedModelBox tail_tip;
    private final AdvancedModelBox body_hips;
    private final AdvancedModelBox claw_left_1;
    private final AdvancedModelBox claw_left_2;
    private final AdvancedModelBox claw_left_3;
    private final AdvancedModelBox claw_right_1;
    private final AdvancedModelBox claw_right_2;
    private final AdvancedModelBox claw_right_3;

    public ModelGroundSloth() {
        this.texWidth = 128;
        this.texHeight = 128;

        this.body_main = new AdvancedModelBox(this, 0, 0);
        this.body_main.setRotationPoint(0F, -3F, 0F);
        this.body_main.addBox(-7F, -7F, -11F, 14F, 14F, 22F, 0F);
        this.arm_left_1 = new AdvancedModelBox(this, 48, 36);
        this.arm_left_1.setRotationPoint(4.2F, 5F, -8F);
        this.arm_left_1.addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, 0F);
        this.arm_left_2 = new AdvancedModelBox(this, 68, 36);
        this.arm_left_2.setRotationPoint(0F, 10F, 0F);
        this.arm_left_2.addBox(-2F, 0F, -2F, 4F, 10F, 4F, 0F);
        this.arm_left_3 = new AdvancedModelBox(this, 46, 56);
        this.arm_left_3.setRotationPoint(0F, 9F, 0F);
        this.arm_left_3.addBox(-2F, 0F, -7F, 4F, 3F, 8F, 0F);
        this.arm_right_1 = new AdvancedModelBox(this, 48, 36);
        this.arm_right_1.mirror = true;
        this.arm_right_1.setRotationPoint(-4.2F, 5F, -8F);
        this.arm_right_1.addBox(-2.5F, 0F, -2.5F, 5F, 11F, 5F, 0F);
        this.arm_right_2 = new AdvancedModelBox(this, 68, 36);
        this.arm_right_2.mirror = true;
        this.arm_right_2.setRotationPoint(0F, 10F, 0F);
        this.arm_right_2.addBox(-2F, 0F, -2F, 4F, 10F, 4F, 0F);
        this.arm_right_3 = new AdvancedModelBox(this, 46, 56);
        this.arm_right_3.mirror = true;
        this.arm_right_3.setRotationPoint(0F, 9F, 0F);
        this.arm_right_3.addBox(-2F, 0F, -7F, 4F, 3F, 8F, 0F);
        this.leg_left_1 = new AdvancedModelBox(this, 24, 36);
        this.leg_left_1.setRotationPoint(4.5F, 5F, 8F);
        this.leg_left_1.addBox(-3F, 0F, -3F, 6F, 13F, 6F, 0F);
        this.setRotateAngle(leg_left_1, 0.05F, 0F, 0F);
        this.leg_left_2 = new AdvancedModelBox(this, 0, 56);
        this.leg_left_2.setRotationPoint(0F, 12F, 0F);
        this.leg_left_2.addBox(-2.5F, 0F, -2.5F, 5F, 8F, 5F, 0F);
        this.leg_left_3 = new AdvancedModelBox(this, 70, 56);
        this.leg_left_3.setRotationPoint(0F, 7F, 0F);
        this.leg_left_3.addBox(-2.5F, 0F, -6F, 5F, 3F, 8F, 0F);
        this.leg_right_1 = new AdvancedModelBox(this, 24, 36);
        this.leg_right_1.mirror = true;
        this.leg_right_1.setRotationPoint(-4.5F, 5F, 8F);
        this.leg_right_1.addBox(-3F, 0F, -3F, 6F, 13F, 6F, 0F);
        this.setRotateAngle(leg_right_1, 0.05F, 0F, 0F);
        this.leg_right_2 = new AdvancedModelBox(this, 0, 56);
        this.leg_right_2.mirror = true;
        this.leg_right_2.setRotationPoint(0F, 12F, 0F);
        this.leg_right_2.addBox(-2.5F, 0F, -2.5F, 5F, 8F, 5F, 0F);
        this.leg_right_3 = new AdvancedModelBox(this, 70, 56);
        this.leg_right_3.mirror = true;
        this.leg_right_3.setRotationPoint(0F, 7F, 0F);
        this.leg_right_3.addBox(-2.5F, 0F, -6F, 5F, 3F, 8F, 0F);
        this.head_neck = new AdvancedModelBox(this, 84, 36);
        this.head_neck.setRotationPoint(0F, -1F, -10F);
        this.head_neck.addBox(-3F, -3.5F, -6F, 6F, 7F, 6F, 0F);
        this.head_main = new AdvancedModelBox(this, 20, 56);
        this.head_main.setRotationPoint(0F, 0F, -5F);
        this.head_main.addBox(-2.5F, -3F, -7F, 5F, 5F, 8F, 0F);
        this.setRotateAngle(head_main, 0.3F, 0F, 0F);
        this.head_snout = new AdvancedModelBox(this, 60, 90);
        this.head_snout.setRotationPoint(0F, 0.5F, -7F);
        this.head_snout.addBox(-2F, -2F, -4F, 4F, 4F, 4F, 0F);
        this.eye_left = new AdvancedModelBox(this, 116, 0);
        this.eye_left.mirror = true;
        this.eye_left.setRotationPoint(2.52F, -1.5F, -4.5F);
        this.eye_left.addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F, 0F);
        this.eye_right = new AdvancedModelBox(this, 116, 0);
        this.eye_right.setRotationPoint(-2.52F, -1.5F, -4.5F);
        this.eye_right.addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F, 0F);
        this.head_ear_left = new AdvancedModelBox(this, 14, 69);
        this.head_ear_left.setRotationPoint(2F, -1.5F, 0F);
        this.head_ear_left.addBox(0F, -1F, 0F, 1F, 2F, 2F, 0F);
        this.setRotateAngle(head_ear_left, 0F, 0.2F, 0F);
        this.head_ear_right = new AdvancedModelBox(this, 14, 69);
        this.head_ear_right.mirror = true;
        this.head_ear_right.setRotationPoint(-2F, -1.5F, 0F);
        this.head_ear_right.addBox(-1F, -1F, 0F, 1F, 2F, 2F, 0F);
        this.setRotateAngle(head_ear_right, 0F, -0.2F, 0F);
        this.tail = new AdvancedModelBox(this, 0, 109);
        this.tail.setRotationPoint(0F, -2F, 15F);
        this.tail.addBox(-4F, 0F, -4F, 8F, 11F, 8F, 0F);
        this.setRotateAngle(tail, 0.75F, 0F, 0F);
        this.tail_tip = new AdvancedModelBox(this, 32, 109);
        this.tail_tip.setRotationPoint(0F, 10F, 0F);
        this.tail_tip.addBox(-2.5F, 0F, -2.5F, 5F, 9F, 5F, 0F);
        this.setRotateAngle(tail_tip, 0.35F, 0F, 0F);
        this.body_hips = new AdvancedModelBox(this, 0, 80);
        this.body_hips.setRotationPoint(0F, 0F, 0F);
        this.body_hips.addBox(-8F, -8.5F, 1F, 16F, 16F, 13F, 0F);
        this.claw_left_1 = new AdvancedModelBox(this, 60, 80);
        this.claw_left_1.setRotationPoint(-1.3F, 1.5F, -6.5F);
        this.claw_left_1.addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F, 0F);
        this.setRotateAngle(claw_left_1, 0.6F, -0.15F, 0F);
        this.claw_left_2 = new AdvancedModelBox(this, 60, 80);
        this.claw_left_2.setRotationPoint(0F, 1.5F, -6.5F);
        this.claw_left_2.addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F, 0F);
        this.setRotateAngle(claw_left_2, 0.6F, 0F, 0F);
        this.claw_left_3 = new AdvancedModelBox(this, 60, 80);
        this.claw_left_3.setRotationPoint(1.3F, 1.5F, -6.5F);
        this.claw_left_3.addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F, 0F);
        this.setRotateAngle(claw_left_3, 0.6F, 0.15F, 0F);
        this.claw_right_1 = new AdvancedModelBox(this, 60, 80);
        this.claw_right_1.mirror = true;
        this.claw_right_1.setRotationPoint(-1.3F, 1.5F, -6.5F);
        this.claw_right_1.addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F, 0F);
        this.setRotateAngle(claw_right_1, 0.6F, -0.15F, 0F);
        this.claw_right_2 = new AdvancedModelBox(this, 60, 80);
        this.claw_right_2.mirror = true;
        this.claw_right_2.setRotationPoint(0F, 1.5F, -6.5F);
        this.claw_right_2.addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F, 0F);
        this.setRotateAngle(claw_right_2, 0.6F, 0F, 0F);
        this.claw_right_3 = new AdvancedModelBox(this, 60, 80);
        this.claw_right_3.mirror = true;
        this.claw_right_3.setRotationPoint(1.3F, 1.5F, -6.5F);
        this.claw_right_3.addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F, 0F);
        this.setRotateAngle(claw_right_3, 0.6F, 0.15F, 0F);
        this.skirt_left = new AdvancedModelBox(this, 72, 0);
        this.skirt_left.setRotationPoint(7.3F, 3F, 0F);
        this.skirt_left.addBox(0F, 0F, -11F, 0F, 12F, 22F, 0F);
        this.skirt_right = new AdvancedModelBox(this, 72, 0);
        this.skirt_right.mirror = true;
        this.skirt_right.setRotationPoint(-7.3F, 3F, 0F);
        this.skirt_right.addBox(0F, 0F, -11F, 0F, 12F, 22F, 0F);
        this.mane = new AdvancedModelBox(this, 96, 56);
        this.mane.setRotationPoint(0F, -1F, 0F);
        this.mane.addBox(-3.5F, -4.5F, -6F, 7F, 3F, 7F, 0F);
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
        this.head_main.addChild(this.head_snout);
        this.head_main.addChild(this.eye_left);
        this.head_main.addChild(this.eye_right);
        this.head_main.addChild(this.head_ear_left);
        this.head_main.addChild(this.head_ear_right);
        this.body_main.addChild(this.tail);
        this.tail.addChild(this.tail_tip);
        this.body_main.addChild(this.body_hips);
        this.arm_left_3.addChild(this.claw_left_1);
        this.arm_left_3.addChild(this.claw_left_2);
        this.arm_left_3.addChild(this.claw_left_3);
        this.arm_right_3.addChild(this.claw_right_1);
        this.arm_right_3.addChild(this.claw_right_2);
        this.arm_right_3.addChild(this.claw_right_3);
        this.body_main.addChild(this.skirt_left);
        this.body_main.addChild(this.skirt_right);
        this.head_neck.addChild(this.mane);
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
            head_snout,
            eye_left,
            eye_right,
            head_ear_left,
            head_ear_right,
            tail,
            skirt_left,
            skirt_right,
            mane,
            tail_tip,
            body_hips,
            claw_left_1,
            claw_left_2,
            claw_left_3,
            claw_right_1,
            claw_right_2,
            claw_right_3
        );
    }

    private static boolean in(int[] set, int v) {
        for (int s : set) {
            if (s == v) return true;
        }
        return false;
    }

    public void setupAnim(EntityGroundSloth mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        float globalSpeed = 1.5f;
        float globalDegree = 1f;
        float f = limbSwing / 2;
        limbSwingAmount = Math.min(0.4F, limbSwingAmount);
        float gait = 0.6F;
        // Breathing
        this.body_main.setScale((float) (1.0F + Math.sin(ageInTicks / 20) * 0.03F), (float) (1.0F + Math.sin(ageInTicks / 16) * 0.03F), 1.0F);
        walk(head_neck, 0.4f * globalSpeed, 0.03f, false, 2.8F, 0.0F, ageInTicks / 20, 2);
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
            this.progressPosition(body_main, rest, 0.0F, 14F, 0.0F, 40);
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
