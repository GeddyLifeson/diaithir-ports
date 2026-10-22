package untamedwilds.client.model;

import com.google.common.collect.ImmutableList;
import untamedwilds.citadel.client.model.AdvancedEntityModel;
import untamedwilds.citadel.client.model.AdvancedModelBox;
import untamedwilds.citadel.client.model.ModelAnimator;
import untamedwilds.citadel.client.model.basic.BasicModelPart;
import net.minecraft.util.Mth;
import untamedwilds.entity.mammal.EntityCervid;

/** Code-built model generated for the cervid body plan (see UW_SPECIES.csv for the species it serves). */
public class ModelCervid extends AdvancedEntityModel<EntityCervid> {

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
    private final AdvancedModelBox moose_nose;
    private final AdvancedModelBox shoulder_hump;
    private final AdvancedModelBox bell;
    private final AdvancedModelBox neck_mane;
    private final AdvancedModelBox as_beam_left;
    private final AdvancedModelBox as_beam_right;
    private final AdvancedModelBox as_tine0_left;
    private final AdvancedModelBox as_tine0_right;
    private final AdvancedModelBox as_tine1_left;
    private final AdvancedModelBox as_tine1_right;
    private final AdvancedModelBox al_beam_left;
    private final AdvancedModelBox al_beam_right;
    private final AdvancedModelBox al_tine0_left;
    private final AdvancedModelBox al_tine0_right;
    private final AdvancedModelBox al_tine1_left;
    private final AdvancedModelBox al_tine1_right;
    private final AdvancedModelBox al_tine2_left;
    private final AdvancedModelBox al_tine2_right;
    private final AdvancedModelBox al_tine3_left;
    private final AdvancedModelBox al_tine3_right;
    private final AdvancedModelBox ar_beam_left;
    private final AdvancedModelBox ar_beam_right;
    private final AdvancedModelBox ar_tine0_left;
    private final AdvancedModelBox ar_tine0_right;
    private final AdvancedModelBox ar_tine1_left;
    private final AdvancedModelBox ar_tine1_right;
    private final AdvancedModelBox ar_tine2_left;
    private final AdvancedModelBox ar_tine2_right;
    private final AdvancedModelBox ar_tine3_left;
    private final AdvancedModelBox ar_tine3_right;
    private final AdvancedModelBox pm_palm_left;
    private final AdvancedModelBox pm_palm_right;
    private final AdvancedModelBox pm_pt0_left;
    private final AdvancedModelBox pm_pt0_right;
    private final AdvancedModelBox pm_pt1_left;
    private final AdvancedModelBox pm_pt1_right;
    private final AdvancedModelBox pm_pt2_left;
    private final AdvancedModelBox pm_pt2_right;
    private final AdvancedModelBox pm_pt3_left;
    private final AdvancedModelBox pm_pt3_right;
    private final AdvancedModelBox pm_pt4_left;
    private final AdvancedModelBox pm_pt4_right;
    private final AdvancedModelBox pm_brow_left;
    private final AdvancedModelBox pm_brow_right;
    private final AdvancedModelBox pg_palm_left;
    private final AdvancedModelBox pg_palm_right;
    private final AdvancedModelBox pg_pt0_left;
    private final AdvancedModelBox pg_pt0_right;
    private final AdvancedModelBox pg_pt1_left;
    private final AdvancedModelBox pg_pt1_right;
    private final AdvancedModelBox pg_pt2_left;
    private final AdvancedModelBox pg_pt2_right;
    private final AdvancedModelBox pg_pt3_left;
    private final AdvancedModelBox pg_pt3_right;
    private final AdvancedModelBox pg_pt4_left;
    private final AdvancedModelBox pg_pt4_right;
    private final AdvancedModelBox pg_pt5_left;
    private final AdvancedModelBox pg_pt5_right;
    private final AdvancedModelBox pg_brow_left;
    private final AdvancedModelBox pg_brow_right;
    private static final int[] SET_0 = {2, 5};
    private static final int[] SET_1 = {1, 3, 4};
    private static final int[] SET_2 = {6, 7, 8};
    private static final int[] SET_3 = {3, 4};
    private static final int[] SET_4 = {1};
    private static final int[] SET_5 = {0};

    public ModelCervid() {
        this.texWidth = 128;
        this.texHeight = 128;

        this.body_main = new AdvancedModelBox(this, 0, 0);
        this.body_main.setRotationPoint(0F, -9F, 0F);
        this.body_main.addBox(-5F, -6F, -13F, 10F, 12F, 26F, 0F);
        this.arm_left_1 = new AdvancedModelBox(this, 38, 38);
        this.arm_left_1.setRotationPoint(2.5F, 4F, -10.5F);
        this.arm_left_1.addBox(-2F, 0F, -2F, 4F, 14F, 4F, 0F);
        this.arm_left_2 = new AdvancedModelBox(this, 54, 38);
        this.arm_left_2.setRotationPoint(0F, 13F, 0F);
        this.arm_left_2.addBox(-1.5F, 0F, -1.5F, 3F, 14F, 3F, 0F);
        this.arm_left_3 = new AdvancedModelBox(this, 60, 87);
        this.arm_left_3.setRotationPoint(0F, 13F, 0F);
        this.arm_left_3.addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, 0F);
        this.arm_right_1 = new AdvancedModelBox(this, 38, 38);
        this.arm_right_1.mirror = true;
        this.arm_right_1.setRotationPoint(-2.5F, 4F, -10.5F);
        this.arm_right_1.addBox(-2F, 0F, -2F, 4F, 14F, 4F, 0F);
        this.arm_right_2 = new AdvancedModelBox(this, 54, 38);
        this.arm_right_2.mirror = true;
        this.arm_right_2.setRotationPoint(0F, 13F, 0F);
        this.arm_right_2.addBox(-1.5F, 0F, -1.5F, 3F, 14F, 3F, 0F);
        this.arm_right_3 = new AdvancedModelBox(this, 60, 87);
        this.arm_right_3.mirror = true;
        this.arm_right_3.setRotationPoint(0F, 13F, 0F);
        this.arm_right_3.addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, 0F);
        this.leg_left_1 = new AdvancedModelBox(this, 72, 0);
        this.leg_left_1.setRotationPoint(2F, 4F, 9.5F);
        this.leg_left_1.addBox(-2.5F, 0F, -3F, 5F, 16F, 6F, 0F);
        this.setRotateAngle(leg_left_1, 0.5F, 0F, 0F);
        this.leg_left_2 = new AdvancedModelBox(this, 66, 38);
        this.leg_left_2.setRotationPoint(0F, 15F, 0F);
        this.leg_left_2.addBox(-1.5F, 0F, -1.5F, 3F, 14F, 3F, 0F);
        this.setRotateAngle(leg_left_2, -0.9F, 0F, 0F);
        this.leg_left_3 = new AdvancedModelBox(this, 8, 87);
        this.leg_left_3.setRotationPoint(0F, 13F, 0F);
        this.leg_left_3.addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F, 0F);
        this.setRotateAngle(leg_left_3, 0.5F, 0F, 0F);
        this.leg_right_1 = new AdvancedModelBox(this, 72, 0);
        this.leg_right_1.mirror = true;
        this.leg_right_1.setRotationPoint(-2F, 4F, 9.5F);
        this.leg_right_1.addBox(-2.5F, 0F, -3F, 5F, 16F, 6F, 0F);
        this.setRotateAngle(leg_right_1, 0.5F, 0F, 0F);
        this.leg_right_2 = new AdvancedModelBox(this, 66, 38);
        this.leg_right_2.mirror = true;
        this.leg_right_2.setRotationPoint(0F, 15F, 0F);
        this.leg_right_2.addBox(-1.5F, 0F, -1.5F, 3F, 14F, 3F, 0F);
        this.setRotateAngle(leg_right_2, -0.9F, 0F, 0F);
        this.leg_right_3 = new AdvancedModelBox(this, 8, 87);
        this.leg_right_3.mirror = true;
        this.leg_right_3.setRotationPoint(0F, 13F, 0F);
        this.leg_right_3.addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F, 0F);
        this.setRotateAngle(leg_right_3, 0.5F, 0F, 0F);
        this.head_neck = new AdvancedModelBox(this, 0, 38);
        this.head_neck.setRotationPoint(0F, -0.5F, -12F);
        this.head_neck.addBox(-2.5F, -3.5F, -14F, 5F, 7F, 14F, 0F);
        this.setRotateAngle(head_neck, -0.8F, 0F, 0F);
        this.head_main = new AdvancedModelBox(this, 94, 38);
        this.head_main.setRotationPoint(0F, 0F, -13F);
        this.head_main.addBox(-2.5F, -3F, -8F, 5F, 6F, 9F, 0F);
        this.setRotateAngle(head_main, 1.15F, 0F, 0F);
        this.head_snout = new AdvancedModelBox(this, 74, 74);
        this.head_snout.setRotationPoint(0F, 1.2F, -8F);
        this.head_snout.addBox(-1.75F, -1.75F, -5F, 3.5F, 3.5F, 5F, 0F);
        this.eye_left = new AdvancedModelBox(this, 94, 0);
        this.eye_left.mirror = true;
        this.eye_left.setRotationPoint(2.52F, -1.5F, -5F);
        this.eye_left.addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F, 0F);
        this.eye_right = new AdvancedModelBox(this, 94, 0);
        this.eye_right.setRotationPoint(-2.52F, -1.5F, -5F);
        this.eye_right.addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F, 0F);
        this.head_ear_left = new AdvancedModelBox(this, 110, 74);
        this.head_ear_left.setRotationPoint(2F, -1.5F, 1.5F);
        this.head_ear_left.addBox(0F, -2.5F, 0F, 1F, 5F, 3F, 0F);
        this.setRotateAngle(head_ear_left, 0.2F, 0.6F, 0F);
        this.head_ear_right = new AdvancedModelBox(this, 110, 74);
        this.head_ear_right.mirror = true;
        this.head_ear_right.setRotationPoint(-2F, -1.5F, 1.5F);
        this.head_ear_right.addBox(-1F, -2.5F, 0F, 1F, 5F, 3F, 0F);
        this.setRotateAngle(head_ear_right, 0.2F, -0.6F, 0F);
        this.tail = new AdvancedModelBox(this, 72, 87);
        this.tail.setRotationPoint(0F, -4F, 12.5F);
        this.tail.addBox(-1F, 0F, -1F, 2F, 4F, 2F, 0F);
        this.setRotateAngle(tail, 0.3F, 0F, 0F);
        this.moose_nose = new AdvancedModelBox(this, 92, 74);
        this.moose_nose.setRotationPoint(0F, 1.5F, -10F);
        this.moose_nose.addBox(-2.5F, -2.5F, -4F, 5F, 5F, 4F, 0F);
        this.setRotateAngle(moose_nose, 0.1F, 0F, 0F);
        this.shoulder_hump = new AdvancedModelBox(this, 0, 59);
        this.shoulder_hump.setRotationPoint(0F, -6F, -6F);
        this.shoulder_hump.addBox(-4.5F, -4F, -5F, 9F, 5F, 10F, 0F);
        this.bell = new AdvancedModelBox(this, 118, 74);
        this.bell.setRotationPoint(0F, 3F, -2F);
        this.bell.addBox(-1F, 0F, -1F, 2F, 6F, 2F, 0F);
        this.setRotateAngle(bell, 0.2F, 0F, 0F);
        this.neck_mane = new AdvancedModelBox(this, 0, 74);
        this.neck_mane.setRotationPoint(0F, -2.5F, -1F);
        this.neck_mane.addBox(-0.5F, -5F, -9F, 1F, 4F, 9F, 0F);
        this.as_beam_left = new AdvancedModelBox(this, 20, 74);
        this.as_beam_left.setRotationPoint(2.5F, -3F, 0F);
        this.as_beam_left.addBox(-1F, -9F, -1F, 2F, 9F, 2F, 0F);
        this.setRotateAngle(as_beam_left, -0.25F, 0F, 0.5F);
        this.as_beam_right = new AdvancedModelBox(this, 20, 74);
        this.as_beam_right.mirror = true;
        this.as_beam_right.setRotationPoint(-2.5F, -3F, 0F);
        this.as_beam_right.addBox(-1F, -9F, -1F, 2F, 9F, 2F, 0F);
        this.setRotateAngle(as_beam_right, -0.25F, 0F, -0.5F);
        this.as_tine0_left = new AdvancedModelBox(this, 88, 87);
        this.as_tine0_left.setRotationPoint(0F, -2F, 0F);
        this.as_tine0_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(as_tine0_left, 0.9F, 0F, 0.2F);
        this.as_tine0_right = new AdvancedModelBox(this, 88, 87);
        this.as_tine0_right.mirror = true;
        this.as_tine0_right.setRotationPoint(0F, -2F, 0F);
        this.as_tine0_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(as_tine0_right, 0.9F, 0F, -0.2F);
        this.as_tine1_left = new AdvancedModelBox(this, 120, 87);
        this.as_tine1_left.setRotationPoint(0F, -7F, 0F);
        this.as_tine1_left.addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, 0F);
        this.setRotateAngle(as_tine1_left, -0.4F, 0F, 0.3F);
        this.as_tine1_right = new AdvancedModelBox(this, 120, 87);
        this.as_tine1_right.mirror = true;
        this.as_tine1_right.setRotationPoint(0F, -7F, 0F);
        this.as_tine1_right.addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, 0F);
        this.setRotateAngle(as_tine1_right, -0.4F, 0F, -0.3F);
        this.al_beam_left = new AdvancedModelBox(this, 78, 38);
        this.al_beam_left.setRotationPoint(2.5F, -3F, 0F);
        this.al_beam_left.addBox(-1F, -15F, -1F, 2F, 15F, 2F, 0F);
        this.setRotateAngle(al_beam_left, -0.25F, 0F, 0.5F);
        this.al_beam_right = new AdvancedModelBox(this, 78, 38);
        this.al_beam_right.mirror = true;
        this.al_beam_right.setRotationPoint(-2.5F, -3F, 0F);
        this.al_beam_right.addBox(-1F, -15F, -1F, 2F, 15F, 2F, 0F);
        this.setRotateAngle(al_beam_right, -0.25F, 0F, -0.5F);
        this.al_tine0_left = new AdvancedModelBox(this, 20, 87);
        this.al_tine0_left.setRotationPoint(0F, -2F, 0F);
        this.al_tine0_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(al_tine0_left, 0.9F, 0F, 0.1F);
        this.al_tine0_right = new AdvancedModelBox(this, 20, 87);
        this.al_tine0_right.mirror = true;
        this.al_tine0_right.setRotationPoint(0F, -2F, 0F);
        this.al_tine0_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(al_tine0_right, 0.9F, 0F, -0.1F);
        this.al_tine1_left = new AdvancedModelBox(this, 24, 87);
        this.al_tine1_left.setRotationPoint(0F, -5F, 0F);
        this.al_tine1_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(al_tine1_left, 0.8F, 0F, 0.1F);
        this.al_tine1_right = new AdvancedModelBox(this, 24, 87);
        this.al_tine1_right.mirror = true;
        this.al_tine1_right.setRotationPoint(0F, -5F, 0F);
        this.al_tine1_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(al_tine1_right, 0.8F, 0F, -0.1F);
        this.al_tine2_left = new AdvancedModelBox(this, 28, 87);
        this.al_tine2_left.setRotationPoint(0F, -8F, 0F);
        this.al_tine2_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(al_tine2_left, -0.3F, 0F, 0.3F);
        this.al_tine2_right = new AdvancedModelBox(this, 28, 87);
        this.al_tine2_right.mirror = true;
        this.al_tine2_right.setRotationPoint(0F, -8F, 0F);
        this.al_tine2_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(al_tine2_right, -0.3F, 0F, -0.3F);
        this.al_tine3_left = new AdvancedModelBox(this, 92, 87);
        this.al_tine3_left.setRotationPoint(0F, -13F, 0F);
        this.al_tine3_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(al_tine3_left, -0.4F, 0F, 0.4F);
        this.al_tine3_right = new AdvancedModelBox(this, 92, 87);
        this.al_tine3_right.mirror = true;
        this.al_tine3_right.setRotationPoint(0F, -13F, 0F);
        this.al_tine3_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(al_tine3_right, -0.4F, 0F, -0.4F);
        this.ar_beam_left = new AdvancedModelBox(this, 86, 38);
        this.ar_beam_left.setRotationPoint(2.5F, -3F, 0F);
        this.ar_beam_left.addBox(-1F, -15F, -1F, 2F, 15F, 2F, 0F);
        this.setRotateAngle(ar_beam_left, -0.25F, 0F, 0.5F);
        this.ar_beam_right = new AdvancedModelBox(this, 86, 38);
        this.ar_beam_right.mirror = true;
        this.ar_beam_right.setRotationPoint(-2.5F, -3F, 0F);
        this.ar_beam_right.addBox(-1F, -15F, -1F, 2F, 15F, 2F, 0F);
        this.setRotateAngle(ar_beam_right, -0.25F, 0F, -0.5F);
        this.ar_tine0_left = new AdvancedModelBox(this, 32, 87);
        this.ar_tine0_left.setRotationPoint(0F, -1F, 0F);
        this.ar_tine0_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(ar_tine0_left, 0.9F, 0F, 0.1F);
        this.ar_tine0_right = new AdvancedModelBox(this, 32, 87);
        this.ar_tine0_right.mirror = true;
        this.ar_tine0_right.setRotationPoint(0F, -1F, 0F);
        this.ar_tine0_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(ar_tine0_right, 0.9F, 0F, -0.1F);
        this.ar_tine1_left = new AdvancedModelBox(this, 80, 87);
        this.ar_tine1_left.setRotationPoint(0F, -4F, 0F);
        this.ar_tine1_left.addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F, 0F);
        this.setRotateAngle(ar_tine1_left, 0.7F, 0F, 0.1F);
        this.ar_tine1_right = new AdvancedModelBox(this, 80, 87);
        this.ar_tine1_right.mirror = true;
        this.ar_tine1_right.setRotationPoint(0F, -4F, 0F);
        this.ar_tine1_right.addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F, 0F);
        this.setRotateAngle(ar_tine1_right, 0.7F, 0F, -0.1F);
        this.ar_tine2_left = new AdvancedModelBox(this, 84, 87);
        this.ar_tine2_left.setRotationPoint(0F, -10F, 0F);
        this.ar_tine2_left.addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F, 0F);
        this.setRotateAngle(ar_tine2_left, -0.6F, 0F, 0.3F);
        this.ar_tine2_right = new AdvancedModelBox(this, 84, 87);
        this.ar_tine2_right.mirror = true;
        this.ar_tine2_right.setRotationPoint(0F, -10F, 0F);
        this.ar_tine2_right.addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F, 0F);
        this.setRotateAngle(ar_tine2_right, -0.6F, 0F, -0.3F);
        this.ar_tine3_left = new AdvancedModelBox(this, 96, 87);
        this.ar_tine3_left.setRotationPoint(0F, -14F, 0F);
        this.ar_tine3_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(ar_tine3_left, -0.4F, 0F, 0.3F);
        this.ar_tine3_right = new AdvancedModelBox(this, 96, 87);
        this.ar_tine3_right.mirror = true;
        this.ar_tine3_right.setRotationPoint(0F, -14F, 0F);
        this.ar_tine3_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(ar_tine3_right, -0.4F, 0F, -0.3F);
        this.pm_palm_left = new AdvancedModelBox(this, 28, 74);
        this.pm_palm_left.setRotationPoint(2.5F, -3F, 0F);
        this.pm_palm_left.addBox(0F, -1F, -5F, 13F, 1F, 10F, 0F);
        this.setRotateAngle(pm_palm_left, 0F, 0F, -0.2F);
        this.pm_palm_right = new AdvancedModelBox(this, 28, 74);
        this.pm_palm_right.mirror = true;
        this.pm_palm_right.setRotationPoint(-2.5F, -3F, 0F);
        this.pm_palm_right.addBox(-13F, -1F, -5F, 13F, 1F, 10F, 0F);
        this.setRotateAngle(pm_palm_right, 0F, 0F, 0.2F);
        this.pm_pt0_left = new AdvancedModelBox(this, 100, 87);
        this.pm_pt0_left.setRotationPoint(2F, 0F, -5F);
        this.pm_pt0_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt0_left, 0.7F, 0F, 0F);
        this.pm_pt0_right = new AdvancedModelBox(this, 100, 87);
        this.pm_pt0_right.mirror = true;
        this.pm_pt0_right.setRotationPoint(-2F, 0F, -5F);
        this.pm_pt0_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt0_right, 0.7F, 0F, 0F);
        this.pm_pt1_left = new AdvancedModelBox(this, 104, 87);
        this.pm_pt1_left.setRotationPoint(4.5F, 0F, -5F);
        this.pm_pt1_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt1_left, 0.7F, 0F, 0F);
        this.pm_pt1_right = new AdvancedModelBox(this, 104, 87);
        this.pm_pt1_right.mirror = true;
        this.pm_pt1_right.setRotationPoint(-4.5F, 0F, -5F);
        this.pm_pt1_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt1_right, 0.7F, 0F, 0F);
        this.pm_pt2_left = new AdvancedModelBox(this, 108, 87);
        this.pm_pt2_left.setRotationPoint(7F, 0F, -5F);
        this.pm_pt2_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt2_left, 0.7F, 0F, 0F);
        this.pm_pt2_right = new AdvancedModelBox(this, 108, 87);
        this.pm_pt2_right.mirror = true;
        this.pm_pt2_right.setRotationPoint(-7F, 0F, -5F);
        this.pm_pt2_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt2_right, 0.7F, 0F, 0F);
        this.pm_pt3_left = new AdvancedModelBox(this, 112, 87);
        this.pm_pt3_left.setRotationPoint(9.5F, 0F, -5F);
        this.pm_pt3_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt3_left, 0.7F, 0F, 0F);
        this.pm_pt3_right = new AdvancedModelBox(this, 112, 87);
        this.pm_pt3_right.mirror = true;
        this.pm_pt3_right.setRotationPoint(-9.5F, 0F, -5F);
        this.pm_pt3_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt3_right, 0.7F, 0F, 0F);
        this.pm_pt4_left = new AdvancedModelBox(this, 116, 87);
        this.pm_pt4_left.setRotationPoint(12F, 0F, -5F);
        this.pm_pt4_left.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt4_left, 0.7F, 0F, 0F);
        this.pm_pt4_right = new AdvancedModelBox(this, 116, 87);
        this.pm_pt4_right.mirror = true;
        this.pm_pt4_right.setRotationPoint(-12F, 0F, -5F);
        this.pm_pt4_right.addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F, 0F);
        this.setRotateAngle(pm_pt4_right, 0.7F, 0F, 0F);
        this.pm_brow_left = new AdvancedModelBox(this, 0, 87);
        this.pm_brow_left.setRotationPoint(2F, -3F, -1F);
        this.pm_brow_left.addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F, 0F);
        this.setRotateAngle(pm_brow_left, 0.9F, 0F, 0.3F);
        this.pm_brow_right = new AdvancedModelBox(this, 0, 87);
        this.pm_brow_right.mirror = true;
        this.pm_brow_right.setRotationPoint(-2F, -3F, -1F);
        this.pm_brow_right.addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F, 0F);
        this.setRotateAngle(pm_brow_right, 0.9F, 0F, -0.3F);
        this.pg_palm_left = new AdvancedModelBox(this, 38, 59);
        this.pg_palm_left.setRotationPoint(2.5F, -3F, 0F);
        this.pg_palm_left.addBox(0F, -1F, -7F, 22F, 1F, 14F, 0F);
        this.setRotateAngle(pg_palm_left, 0F, 0F, -0.2F);
        this.pg_palm_right = new AdvancedModelBox(this, 38, 59);
        this.pg_palm_right.mirror = true;
        this.pg_palm_right.setRotationPoint(-2.5F, -3F, 0F);
        this.pg_palm_right.addBox(-22F, -1F, -7F, 22F, 1F, 14F, 0F);
        this.setRotateAngle(pg_palm_right, 0F, 0F, 0.2F);
        this.pg_pt0_left = new AdvancedModelBox(this, 36, 87);
        this.pg_pt0_left.setRotationPoint(2F, 0F, -7F);
        this.pg_pt0_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt0_left, 0.7F, 0F, 0F);
        this.pg_pt0_right = new AdvancedModelBox(this, 36, 87);
        this.pg_pt0_right.mirror = true;
        this.pg_pt0_right.setRotationPoint(-2F, 0F, -7F);
        this.pg_pt0_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt0_right, 0.7F, 0F, 0F);
        this.pg_pt1_left = new AdvancedModelBox(this, 40, 87);
        this.pg_pt1_left.setRotationPoint(5.8F, 0F, -7F);
        this.pg_pt1_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt1_left, 0.7F, 0F, 0F);
        this.pg_pt1_right = new AdvancedModelBox(this, 40, 87);
        this.pg_pt1_right.mirror = true;
        this.pg_pt1_right.setRotationPoint(-5.8F, 0F, -7F);
        this.pg_pt1_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt1_right, 0.7F, 0F, 0F);
        this.pg_pt2_left = new AdvancedModelBox(this, 44, 87);
        this.pg_pt2_left.setRotationPoint(9.6F, 0F, -7F);
        this.pg_pt2_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt2_left, 0.7F, 0F, 0F);
        this.pg_pt2_right = new AdvancedModelBox(this, 44, 87);
        this.pg_pt2_right.mirror = true;
        this.pg_pt2_right.setRotationPoint(-9.6F, 0F, -7F);
        this.pg_pt2_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt2_right, 0.7F, 0F, 0F);
        this.pg_pt3_left = new AdvancedModelBox(this, 48, 87);
        this.pg_pt3_left.setRotationPoint(13.4F, 0F, -7F);
        this.pg_pt3_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt3_left, 0.7F, 0F, 0F);
        this.pg_pt3_right = new AdvancedModelBox(this, 48, 87);
        this.pg_pt3_right.mirror = true;
        this.pg_pt3_right.setRotationPoint(-13.4F, 0F, -7F);
        this.pg_pt3_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt3_right, 0.7F, 0F, 0F);
        this.pg_pt4_left = new AdvancedModelBox(this, 52, 87);
        this.pg_pt4_left.setRotationPoint(17.2F, 0F, -7F);
        this.pg_pt4_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt4_left, 0.7F, 0F, 0F);
        this.pg_pt4_right = new AdvancedModelBox(this, 52, 87);
        this.pg_pt4_right.mirror = true;
        this.pg_pt4_right.setRotationPoint(-17.2F, 0F, -7F);
        this.pg_pt4_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt4_right, 0.7F, 0F, 0F);
        this.pg_pt5_left = new AdvancedModelBox(this, 56, 87);
        this.pg_pt5_left.setRotationPoint(21F, 0F, -7F);
        this.pg_pt5_left.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt5_left, 0.7F, 0F, 0F);
        this.pg_pt5_right = new AdvancedModelBox(this, 56, 87);
        this.pg_pt5_right.mirror = true;
        this.pg_pt5_right.setRotationPoint(-21F, 0F, -7F);
        this.pg_pt5_right.addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, 0F);
        this.setRotateAngle(pg_pt5_right, 0.7F, 0F, 0F);
        this.pg_brow_left = new AdvancedModelBox(this, 4, 87);
        this.pg_brow_left.setRotationPoint(2F, -3F, -1F);
        this.pg_brow_left.addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F, 0F);
        this.setRotateAngle(pg_brow_left, 0.9F, 0F, 0.3F);
        this.pg_brow_right = new AdvancedModelBox(this, 4, 87);
        this.pg_brow_right.mirror = true;
        this.pg_brow_right.setRotationPoint(-2F, -3F, -1F);
        this.pg_brow_right.addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F, 0F);
        this.setRotateAngle(pg_brow_right, 0.9F, 0F, -0.3F);
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
        this.head_main.addChild(this.moose_nose);
        this.body_main.addChild(this.shoulder_hump);
        this.head_neck.addChild(this.bell);
        this.head_neck.addChild(this.neck_mane);
        this.head_main.addChild(this.as_beam_left);
        this.head_main.addChild(this.as_beam_right);
        this.as_beam_left.addChild(this.as_tine0_left);
        this.as_beam_right.addChild(this.as_tine0_right);
        this.as_beam_left.addChild(this.as_tine1_left);
        this.as_beam_right.addChild(this.as_tine1_right);
        this.head_main.addChild(this.al_beam_left);
        this.head_main.addChild(this.al_beam_right);
        this.al_beam_left.addChild(this.al_tine0_left);
        this.al_beam_right.addChild(this.al_tine0_right);
        this.al_beam_left.addChild(this.al_tine1_left);
        this.al_beam_right.addChild(this.al_tine1_right);
        this.al_beam_left.addChild(this.al_tine2_left);
        this.al_beam_right.addChild(this.al_tine2_right);
        this.al_beam_left.addChild(this.al_tine3_left);
        this.al_beam_right.addChild(this.al_tine3_right);
        this.head_main.addChild(this.ar_beam_left);
        this.head_main.addChild(this.ar_beam_right);
        this.ar_beam_left.addChild(this.ar_tine0_left);
        this.ar_beam_right.addChild(this.ar_tine0_right);
        this.ar_beam_left.addChild(this.ar_tine1_left);
        this.ar_beam_right.addChild(this.ar_tine1_right);
        this.ar_beam_left.addChild(this.ar_tine2_left);
        this.ar_beam_right.addChild(this.ar_tine2_right);
        this.ar_beam_left.addChild(this.ar_tine3_left);
        this.ar_beam_right.addChild(this.ar_tine3_right);
        this.head_main.addChild(this.pm_palm_left);
        this.head_main.addChild(this.pm_palm_right);
        this.pm_palm_left.addChild(this.pm_pt0_left);
        this.pm_palm_right.addChild(this.pm_pt0_right);
        this.pm_palm_left.addChild(this.pm_pt1_left);
        this.pm_palm_right.addChild(this.pm_pt1_right);
        this.pm_palm_left.addChild(this.pm_pt2_left);
        this.pm_palm_right.addChild(this.pm_pt2_right);
        this.pm_palm_left.addChild(this.pm_pt3_left);
        this.pm_palm_right.addChild(this.pm_pt3_right);
        this.pm_palm_left.addChild(this.pm_pt4_left);
        this.pm_palm_right.addChild(this.pm_pt4_right);
        this.head_main.addChild(this.pm_brow_left);
        this.head_main.addChild(this.pm_brow_right);
        this.head_main.addChild(this.pg_palm_left);
        this.head_main.addChild(this.pg_palm_right);
        this.pg_palm_left.addChild(this.pg_pt0_left);
        this.pg_palm_right.addChild(this.pg_pt0_right);
        this.pg_palm_left.addChild(this.pg_pt1_left);
        this.pg_palm_right.addChild(this.pg_pt1_right);
        this.pg_palm_left.addChild(this.pg_pt2_left);
        this.pg_palm_right.addChild(this.pg_pt2_right);
        this.pg_palm_left.addChild(this.pg_pt3_left);
        this.pg_palm_right.addChild(this.pg_pt3_right);
        this.pg_palm_left.addChild(this.pg_pt4_left);
        this.pg_palm_right.addChild(this.pg_pt4_right);
        this.pg_palm_left.addChild(this.pg_pt5_left);
        this.pg_palm_right.addChild(this.pg_pt5_right);
        this.head_main.addChild(this.pg_brow_left);
        this.head_main.addChild(this.pg_brow_right);
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
            moose_nose,
            shoulder_hump,
            bell,
            neck_mane,
            as_beam_left,
            as_beam_right,
            as_tine0_left,
            as_tine0_right,
            as_tine1_left,
            as_tine1_right,
            al_beam_left,
            al_beam_right,
            al_tine0_left,
            al_tine0_right,
            al_tine1_left,
            al_tine1_right,
            al_tine2_left,
            al_tine2_right,
            al_tine3_left,
            al_tine3_right,
            ar_beam_left,
            ar_beam_right,
            ar_tine0_left,
            ar_tine0_right,
            ar_tine1_left,
            ar_tine1_right,
            ar_tine2_left,
            ar_tine2_right,
            ar_tine3_left,
            ar_tine3_right,
            pm_palm_left,
            pm_palm_right,
            pm_pt0_left,
            pm_pt0_right,
            pm_pt1_left,
            pm_pt1_right,
            pm_pt2_left,
            pm_pt2_right,
            pm_pt3_left,
            pm_pt3_right,
            pm_pt4_left,
            pm_pt4_right,
            pm_brow_left,
            pm_brow_right,
            pg_palm_left,
            pg_palm_right,
            pg_pt0_left,
            pg_pt0_right,
            pg_pt1_left,
            pg_pt1_right,
            pg_pt2_left,
            pg_pt2_right,
            pg_pt3_left,
            pg_pt3_right,
            pg_pt4_left,
            pg_pt4_right,
            pg_pt5_left,
            pg_pt5_right,
            pg_brow_left,
            pg_brow_right
        );
    }

    private static boolean in(int[] set, int v) {
        for (int s : set) {
            if (s == v) return true;
        }
        return false;
    }

    public void setupAnim(EntityCervid mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        this.moose_nose.showModel = in(SET_0, mob.getVariant());
        this.shoulder_hump.showModel = in(SET_0, mob.getVariant());
        this.bell.showModel = in(SET_0, mob.getVariant());
        this.neck_mane.showModel = in(SET_1, mob.getVariant());
        this.as_beam_left.showModel = in(SET_2, mob.getVariant()) && mob.isMale();
        this.as_beam_right.showModel = in(SET_2, mob.getVariant()) && mob.isMale();
        this.as_tine0_left.showModel = in(SET_2, mob.getVariant()) && mob.isMale();
        this.as_tine0_right.showModel = in(SET_2, mob.getVariant()) && mob.isMale();
        this.as_tine1_left.showModel = in(SET_2, mob.getVariant()) && mob.isMale();
        this.as_tine1_right.showModel = in(SET_2, mob.getVariant()) && mob.isMale();
        this.al_beam_left.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_beam_right.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine0_left.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine0_right.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine1_left.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine1_right.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine2_left.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine2_right.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine3_left.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.al_tine3_right.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.ar_beam_left.showModel = in(SET_4, mob.getVariant());
        this.ar_beam_right.showModel = in(SET_4, mob.getVariant());
        this.ar_tine0_left.showModel = in(SET_4, mob.getVariant());
        this.ar_tine0_right.showModel = in(SET_4, mob.getVariant());
        this.ar_tine1_left.showModel = in(SET_4, mob.getVariant());
        this.ar_tine1_right.showModel = in(SET_4, mob.getVariant());
        this.ar_tine2_left.showModel = in(SET_4, mob.getVariant());
        this.ar_tine2_right.showModel = in(SET_4, mob.getVariant());
        this.ar_tine3_left.showModel = in(SET_4, mob.getVariant());
        this.ar_tine3_right.showModel = in(SET_4, mob.getVariant());
        this.pm_palm_left.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_palm_right.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt0_left.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt0_right.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt1_left.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt1_right.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt2_left.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt2_right.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt3_left.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt3_right.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt4_left.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_pt4_right.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_brow_left.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pm_brow_right.showModel = in(SET_0, mob.getVariant()) && mob.isMale();
        this.pg_palm_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_palm_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt0_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt0_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt1_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt1_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt2_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt2_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt3_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt3_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt4_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt4_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt5_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_pt5_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_brow_left.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.pg_brow_right.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        float globalSpeed = 1.5f;
        float globalDegree = 1f;
        float f = limbSwing / 2;
        limbSwingAmount = Math.min(0.4F, limbSwingAmount);
        float gait = 1.1F;
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
            this.progressPosition(body_main, rest, 0.0F, 21F, 0.0F, 40);
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
