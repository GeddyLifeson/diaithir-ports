package untamedwilds.client.model;

import com.google.common.collect.ImmutableList;
import untamedwilds.citadel.client.model.AdvancedEntityModel;
import untamedwilds.citadel.client.model.AdvancedModelBox;
import untamedwilds.citadel.client.model.ModelAnimator;
import untamedwilds.citadel.client.model.basic.BasicModelPart;
import net.minecraft.util.Mth;
import untamedwilds.entity.mammal.EntityEquid;

/** Code-built model generated for the equid body plan (see UW_SPECIES.csv for the species it serves). */
public class ModelEquid extends AdvancedEntityModel<EntityEquid> {

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
    private final AdvancedModelBox ear_long_left;
    private final AdvancedModelBox ear_long_right;
    private final AdvancedModelBox mane_long;
    private final AdvancedModelBox mane_erect;
    private final AdvancedModelBox forelock;
    private final AdvancedModelBox tail_tuft;
    private static final int[] SET_0 = {0, 1};
    private static final int[] SET_1 = {2};
    private static final int[] SET_2 = {0};
    private static final int[] SET_3 = {1, 2};

    public ModelEquid() {
        this.texWidth = 128;
        this.texHeight = 128;

        this.body_main = new AdvancedModelBox(this, 0, 0);
        this.body_main.setRotationPoint(0F, -8F, 0F);
        this.body_main.addBox(-6F, -7F, -13F, 12F, 14F, 26F, 0F);
        this.arm_left_1 = new AdvancedModelBox(this, 54, 40);
        this.arm_left_1.setRotationPoint(3.5F, 5F, -10.5F);
        this.arm_left_1.addBox(-2F, 0F, -2F, 4F, 14F, 4F, 0F);
        this.arm_left_2 = new AdvancedModelBox(this, 110, 40);
        this.arm_left_2.setRotationPoint(0F, 13F, 0F);
        this.arm_left_2.addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, 0F);
        this.arm_left_3 = new AdvancedModelBox(this, 78, 61);
        this.arm_left_3.setRotationPoint(0F, 11F, 0F);
        this.arm_left_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.arm_right_1 = new AdvancedModelBox(this, 54, 40);
        this.arm_right_1.mirror = true;
        this.arm_right_1.setRotationPoint(-3.5F, 5F, -10.5F);
        this.arm_right_1.addBox(-2F, 0F, -2F, 4F, 14F, 4F, 0F);
        this.arm_right_2 = new AdvancedModelBox(this, 110, 40);
        this.arm_right_2.mirror = true;
        this.arm_right_2.setRotationPoint(0F, 13F, 0F);
        this.arm_right_2.addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, 0F);
        this.arm_right_3 = new AdvancedModelBox(this, 78, 61);
        this.arm_right_3.mirror = true;
        this.arm_right_3.setRotationPoint(0F, 11F, 0F);
        this.arm_right_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.leg_left_1 = new AdvancedModelBox(this, 0, 40);
        this.leg_left_1.setRotationPoint(3F, 5F, 9.5F);
        this.leg_left_1.addBox(-2.5F, 0F, -3F, 5F, 15F, 6F, 0F);
        this.setRotateAngle(leg_left_1, 0.35F, 0F, 0F);
        this.leg_left_2 = new AdvancedModelBox(this, 0, 61);
        this.leg_left_2.setRotationPoint(0F, 14F, 0F);
        this.leg_left_2.addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, 0F);
        this.setRotateAngle(leg_left_2, -0.6F, 0F, 0F);
        this.leg_left_3 = new AdvancedModelBox(this, 94, 61);
        this.leg_left_3.setRotationPoint(0F, 11F, 0F);
        this.leg_left_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.setRotateAngle(leg_left_3, 0.35F, 0F, 0F);
        this.leg_right_1 = new AdvancedModelBox(this, 0, 40);
        this.leg_right_1.mirror = true;
        this.leg_right_1.setRotationPoint(-3F, 5F, 9.5F);
        this.leg_right_1.addBox(-2.5F, 0F, -3F, 5F, 15F, 6F, 0F);
        this.setRotateAngle(leg_right_1, 0.35F, 0F, 0F);
        this.leg_right_2 = new AdvancedModelBox(this, 0, 61);
        this.leg_right_2.mirror = true;
        this.leg_right_2.setRotationPoint(0F, 14F, 0F);
        this.leg_right_2.addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, 0F);
        this.setRotateAngle(leg_right_2, -0.6F, 0F, 0F);
        this.leg_right_3 = new AdvancedModelBox(this, 94, 61);
        this.leg_right_3.mirror = true;
        this.leg_right_3.setRotationPoint(0F, 11F, 0F);
        this.leg_right_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.setRotateAngle(leg_right_3, 0.35F, 0F, 0F);
        this.head_neck = new AdvancedModelBox(this, 76, 0);
        this.head_neck.setRotationPoint(0F, -0.5F, -12F);
        this.head_neck.addBox(-3F, -4.5F, -13F, 6F, 9F, 13F, 0F);
        this.setRotateAngle(head_neck, -0.75F, 0F, 0F);
        this.head_main = new AdvancedModelBox(this, 82, 40);
        this.head_main.setRotationPoint(0F, 0F, -12F);
        this.head_main.addBox(-2.5F, -3.5F, -8F, 5F, 7F, 9F, 0F);
        this.setRotateAngle(head_main, 1F, 0F, 0F);
        this.head_snout = new AdvancedModelBox(this, 40, 61);
        this.head_snout.setRotationPoint(0F, 1.5F, -8F);
        this.head_snout.addBox(-2F, -2.5F, -6F, 4F, 5F, 6F, 0F);
        this.setRotateAngle(head_snout, 0.05F, 0F, 0F);
        this.eye_left = new AdvancedModelBox(this, 114, 0);
        this.eye_left.mirror = true;
        this.eye_left.setRotationPoint(2.52F, -1.75F, -5F);
        this.eye_left.addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F, 0F);
        this.eye_right = new AdvancedModelBox(this, 114, 0);
        this.eye_right.setRotationPoint(-2.52F, -1.75F, -5F);
        this.eye_right.addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F, 0F);
        this.head_ear_left = new AdvancedModelBox(this, 110, 61);
        this.head_ear_left.setRotationPoint(2F, -2F, 1.5F);
        this.head_ear_left.addBox(0F, -2F, 0F, 1F, 4F, 2F, 0F);
        this.setRotateAngle(head_ear_left, 0.2F, 0.15F, 0F);
        this.head_ear_right = new AdvancedModelBox(this, 110, 61);
        this.head_ear_right.mirror = true;
        this.head_ear_right.setRotationPoint(-2F, -2F, 1.5F);
        this.head_ear_right.addBox(-1F, -2F, 0F, 1F, 4F, 2F, 0F);
        this.setRotateAngle(head_ear_right, 0.2F, -0.15F, 0F);
        this.tail = new AdvancedModelBox(this, 70, 40);
        this.tail.setRotationPoint(0F, -5F, 12.5F);
        this.tail.addBox(-1.5F, 0F, -1.5F, 3F, 15F, 3F, 0F);
        this.setRotateAngle(tail, 0.15F, 0F, 0F);
        this.ear_long_left = new AdvancedModelBox(this, 60, 61);
        this.ear_long_left.setRotationPoint(1.8F, -3.2F, 1.5F);
        this.ear_long_left.addBox(0F, -3F, 0F, 1F, 7F, 2F, 0F);
        this.setRotateAngle(ear_long_left, 0.2F, 0.15F, 0F);
        this.ear_long_right = new AdvancedModelBox(this, 60, 61);
        this.ear_long_right.mirror = true;
        this.ear_long_right.setRotationPoint(-1.8F, -3.2F, 1.5F);
        this.ear_long_right.addBox(-1F, -3F, 0F, 1F, 7F, 2F, 0F);
        this.setRotateAngle(ear_long_right, 0.2F, -0.15F, 0F);
        this.mane_long = new AdvancedModelBox(this, 22, 40);
        this.mane_long.setRotationPoint(0F, -4.5F, 0F);
        this.mane_long.addBox(-1F, 0F, -13F, 2F, 5F, 14F, 0F);
        this.mane_erect = new AdvancedModelBox(this, 12, 61);
        this.mane_erect.setRotationPoint(0F, -3.5F, 0F);
        this.mane_erect.addBox(-1F, -2.5F, -12F, 2F, 3F, 12F, 0F);
        this.forelock = new AdvancedModelBox(this, 116, 61);
        this.forelock.setRotationPoint(0F, -3.5F, 0.5F);
        this.forelock.addBox(-1F, -1F, -1F, 2F, 4F, 2F, 0F);
        this.setRotateAngle(forelock, 0.2F, 0F, 0F);
        this.tail_tuft = new AdvancedModelBox(this, 66, 61);
        this.tail_tuft.setRotationPoint(0F, 13F, 0F);
        this.tail_tuft.addBox(-1.5F, 0F, -1.5F, 3F, 5F, 3F, 0F);
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
        this.head_main.addChild(this.ear_long_left);
        this.head_main.addChild(this.ear_long_right);
        this.head_neck.addChild(this.mane_long);
        this.head_neck.addChild(this.mane_erect);
        this.head_main.addChild(this.forelock);
        this.tail.addChild(this.tail_tuft);
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
            ear_long_left,
            ear_long_right,
            mane_long,
            mane_erect,
            forelock,
            tail_tuft
        );
    }

    private static boolean in(int[] set, int v) {
        for (int s : set) {
            if (s == v) return true;
        }
        return false;
    }

    public void setupAnim(EntityEquid mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        this.head_ear_left.showModel = in(SET_0, mob.getVariant());
        this.head_ear_right.showModel = in(SET_0, mob.getVariant());
        this.ear_long_left.showModel = in(SET_1, mob.getVariant());
        this.ear_long_right.showModel = in(SET_1, mob.getVariant());
        this.mane_long.showModel = in(SET_2, mob.getVariant());
        this.mane_erect.showModel = in(SET_3, mob.getVariant());
        this.forelock.showModel = in(SET_2, mob.getVariant());
        this.tail_tuft.showModel = in(SET_1, mob.getVariant());
        float globalSpeed = 1.5f;
        float globalDegree = 1f;
        float f = limbSwing / 2;
        limbSwingAmount = Math.min(0.4F, limbSwingAmount);
        float gait = 1.2F;
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
            this.progressPosition(body_main, rest, 0.0F, 19F, 0.0F, 40);
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
