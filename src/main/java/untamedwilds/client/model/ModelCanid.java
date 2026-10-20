package untamedwilds.client.model;

import com.google.common.collect.ImmutableList;
import untamedwilds.citadel.client.model.AdvancedEntityModel;
import untamedwilds.citadel.client.model.AdvancedModelBox;
import untamedwilds.citadel.client.model.ModelAnimator;
import untamedwilds.citadel.client.model.basic.BasicModelPart;
import net.minecraft.util.Mth;
import untamedwilds.entity.mammal.EntityCanid;

/** Code-built model generated for the canid body plan (see UW_SPECIES.csv for the species it serves). */
public class ModelCanid extends AdvancedEntityModel<EntityCanid> {

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
    private final AdvancedModelBox ruff;
    private static final int[] SET_0 = {0, 1};

    public ModelCanid() {
        this.texWidth = 128;
        this.texHeight = 64;

        this.body_main = new AdvancedModelBox(this, 0, 0);
        this.body_main.setRotationPoint(0F, 5.5F, 0F);
        this.body_main.addBox(-4F, -4.5F, -8F, 8F, 9F, 16F, 0F);
        this.arm_left_1 = new AdvancedModelBox(this, 48, 25);
        this.arm_left_1.setRotationPoint(2.2F, 2.5F, -6F);
        this.arm_left_1.addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, 0F);
        this.arm_left_2 = new AdvancedModelBox(this, 60, 25);
        this.arm_left_2.setRotationPoint(0F, 7F, 0F);
        this.arm_left_2.addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, 0F);
        this.arm_left_3 = new AdvancedModelBox(this, 110, 25);
        this.arm_left_3.setRotationPoint(0F, 7F, 0F);
        this.arm_left_3.addBox(-1.5F, 0F, -3F, 3F, 2F, 4F, 0F);
        this.arm_right_1 = new AdvancedModelBox(this, 48, 25);
        this.arm_right_1.mirror = true;
        this.arm_right_1.setRotationPoint(-2.2F, 2.5F, -6F);
        this.arm_right_1.addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, 0F);
        this.arm_right_2 = new AdvancedModelBox(this, 60, 25);
        this.arm_right_2.mirror = true;
        this.arm_right_2.setRotationPoint(0F, 7F, 0F);
        this.arm_right_2.addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, 0F);
        this.arm_right_3 = new AdvancedModelBox(this, 110, 25);
        this.arm_right_3.mirror = true;
        this.arm_right_3.setRotationPoint(0F, 7F, 0F);
        this.arm_right_3.addBox(-1.5F, 0F, -3F, 3F, 2F, 4F, 0F);
        this.leg_left_1 = new AdvancedModelBox(this, 48, 0);
        this.leg_left_1.setRotationPoint(1.7F, 2.5F, 5F);
        this.leg_left_1.addBox(-2F, 0F, -2.5F, 4F, 10F, 5F, 0F);
        this.setRotateAngle(leg_left_1, 0.5F, 0F, 0F);
        this.leg_left_2 = new AdvancedModelBox(this, 72, 25);
        this.leg_left_2.setRotationPoint(0F, 9F, 0F);
        this.leg_left_2.addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, 0F);
        this.setRotateAngle(leg_left_2, -0.9F, 0F, 0F);
        this.leg_left_3 = new AdvancedModelBox(this, 0, 38);
        this.leg_left_3.setRotationPoint(0F, 7F, 0F);
        this.leg_left_3.addBox(-1.5F, 0F, -3F, 3F, 2F, 4F, 0F);
        this.setRotateAngle(leg_left_3, 0.5F, 0F, 0F);
        this.leg_right_1 = new AdvancedModelBox(this, 48, 0);
        this.leg_right_1.mirror = true;
        this.leg_right_1.setRotationPoint(-1.7F, 2.5F, 5F);
        this.leg_right_1.addBox(-2F, 0F, -2.5F, 4F, 10F, 5F, 0F);
        this.setRotateAngle(leg_right_1, 0.5F, 0F, 0F);
        this.leg_right_2 = new AdvancedModelBox(this, 72, 25);
        this.leg_right_2.mirror = true;
        this.leg_right_2.setRotationPoint(0F, 9F, 0F);
        this.leg_right_2.addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, 0F);
        this.setRotateAngle(leg_right_2, -0.9F, 0F, 0F);
        this.leg_right_3 = new AdvancedModelBox(this, 0, 38);
        this.leg_right_3.mirror = true;
        this.leg_right_3.setRotationPoint(0F, 7F, 0F);
        this.leg_right_3.addBox(-1.5F, 0F, -3F, 3F, 2F, 4F, 0F);
        this.setRotateAngle(leg_right_3, 0.5F, 0F, 0F);
        this.head_neck = new AdvancedModelBox(this, 0, 25);
        this.head_neck.setRotationPoint(0F, 0F, -7F);
        this.head_neck.addBox(-3F, -3.5F, -6F, 6F, 7F, 6F, 0F);
        this.setRotateAngle(head_neck, -0.45F, 0F, 0F);
        this.head_main = new AdvancedModelBox(this, 24, 25);
        this.head_main.setRotationPoint(0F, 0F, -5F);
        this.head_main.addBox(-3F, -3F, -5F, 6F, 6F, 6F, 0F);
        this.setRotateAngle(head_main, 0.5F, 0F, 0F);
        this.head_snout = new AdvancedModelBox(this, 84, 25);
        this.head_snout.setRotationPoint(0F, 1.2F, -5F);
        this.head_snout.addBox(-1.75F, -1.75F, -5F, 3.5F, 3.5F, 5F, 0F);
        this.eye_left = new AdvancedModelBox(this, 14, 38);
        this.eye_left.mirror = true;
        this.eye_left.setRotationPoint(3.02F, -1.5F, -2F);
        this.eye_left.addBox(0F, -0.5F, -1F, 0F, 1F, 2F, 0F);
        this.eye_right = new AdvancedModelBox(this, 14, 38);
        this.eye_right.setRotationPoint(-3.02F, -1.5F, -2F);
        this.eye_right.addBox(0F, -0.5F, -1F, 0F, 1F, 2F, 0F);
        this.head_ear_left = new AdvancedModelBox(this, 102, 25);
        this.head_ear_left.setRotationPoint(2.5F, -1.5F, 1.5F);
        this.head_ear_left.addBox(0F, -2F, 0F, 1F, 4F, 3F, 0F);
        this.setRotateAngle(head_ear_left, 0F, 0.1F, 0F);
        this.head_ear_right = new AdvancedModelBox(this, 102, 25);
        this.head_ear_right.mirror = true;
        this.head_ear_right.setRotationPoint(-2.5F, -1.5F, 1.5F);
        this.head_ear_right.addBox(-1F, -2F, 0F, 1F, 4F, 3F, 0F);
        this.setRotateAngle(head_ear_right, 0F, -0.1F, 0F);
        this.tail = new AdvancedModelBox(this, 66, 0);
        this.tail.setRotationPoint(0F, -2.5F, 7.5F);
        this.tail.addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, 0F);
        this.setRotateAngle(tail, 0.7F, 0F, 0F);
        this.ruff = new AdvancedModelBox(this, 78, 0);
        this.ruff.setRotationPoint(0F, 0F, -0.5F);
        this.ruff.addBox(-4F, -4F, -6F, 8F, 8F, 7F, 0F);
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
        this.head_neck.addChild(this.ruff);
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
            ruff
        );
    }

    private static boolean in(int[] set, int v) {
        for (int s : set) {
            if (s == v) return true;
        }
        return false;
    }

    public void setupAnim(EntityCanid mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        this.ruff.showModel = in(SET_0, mob.getVariant());
        float globalSpeed = 1.5f;
        float globalDegree = 1f;
        float f = limbSwing / 2;
        limbSwingAmount = Math.min(0.4F, limbSwingAmount);
        float gait = 1.3F;
        // Breathing
        this.body_main.setScale((float) (1.0F + Math.sin(ageInTicks / 20) * 0.03F), (float) (1.0F + Math.sin(ageInTicks / 16) * 0.03F), 1.0F);
        walk(head_neck, 0.4f * globalSpeed, 0.03f, false, 2.8F, 0.0F, ageInTicks / 20, 2);
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
            this.progressPosition(body_main, rest, 0.0F, 8F, 0.0F, 40);
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
