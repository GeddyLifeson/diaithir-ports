package untamedwilds.client.model;

import com.google.common.collect.ImmutableList;
import untamedwilds.citadel.client.model.AdvancedEntityModel;
import untamedwilds.citadel.client.model.AdvancedModelBox;
import untamedwilds.citadel.client.model.ModelAnimator;
import untamedwilds.citadel.client.model.basic.BasicModelPart;
import net.minecraft.util.Mth;
import untamedwilds.entity.mammal.EntityBovid;

/** Code-built model generated for the bovid body plan (see UW_SPECIES.csv for the species it serves). */
public class ModelBovid extends AdvancedEntityModel<EntityBovid> {

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
    private final AdvancedModelBox hl_0_left;
    private final AdvancedModelBox hl_0_right;
    private final AdvancedModelBox hl_1_left;
    private final AdvancedModelBox hl_1_right;
    private final AdvancedModelBox hl_2_left;
    private final AdvancedModelBox hl_2_right;
    private final AdvancedModelBox hlf_0_left;
    private final AdvancedModelBox hlf_0_right;
    private final AdvancedModelBox hlf_1_left;
    private final AdvancedModelBox hlf_1_right;
    private final AdvancedModelBox boss;
    private final AdvancedModelBox md_0_left;
    private final AdvancedModelBox md_0_right;
    private final AdvancedModelBox md_1_left;
    private final AdvancedModelBox md_1_right;
    private final AdvancedModelBox sg_left;
    private final AdvancedModelBox sg_right;
    private final AdvancedModelBox sg_nose;
    private final AdvancedModelBox ib_0_left;
    private final AdvancedModelBox ib_0_right;
    private final AdvancedModelBox ib_1_left;
    private final AdvancedModelBox ib_1_right;
    private final AdvancedModelBox ib_2_left;
    private final AdvancedModelBox ib_2_right;
    private final AdvancedModelBox ibf_0_left;
    private final AdvancedModelBox ibf_0_right;
    private final AdvancedModelBox ib_beard;
    private final AdvancedModelBox bh_0_left;
    private final AdvancedModelBox bh_0_right;
    private final AdvancedModelBox bh_1_left;
    private final AdvancedModelBox bh_1_right;
    private final AdvancedModelBox bh_2_left;
    private final AdvancedModelBox bh_2_right;
    private final AdvancedModelBox bh_3_left;
    private final AdvancedModelBox bh_3_right;
    private final AdvancedModelBox bhf_0_left;
    private final AdvancedModelBox bhf_0_right;
    private final AdvancedModelBox mg_0_left;
    private final AdvancedModelBox mg_0_right;
    private final AdvancedModelBox mg_1_left;
    private final AdvancedModelBox mg_1_right;
    private final AdvancedModelBox mgf_0_left;
    private final AdvancedModelBox mgf_0_right;
    private final AdvancedModelBox mgf_1_left;
    private final AdvancedModelBox mgf_1_right;
    private final AdvancedModelBox skirt_left;
    private final AdvancedModelBox skirt_right;
    private final AdvancedModelBox mane;
    private final AdvancedModelBox hump;
    private final AdvancedModelBox ear_sg_left;
    private final AdvancedModelBox ear_sg_right;
    private static final int[] SET_0 = {0, 1, 3, 4, 5};
    private static final int[] SET_1 = {0};
    private static final int[] SET_2 = {1};
    private static final int[] SET_3 = {2};
    private static final int[] SET_4 = {3};
    private static final int[] SET_5 = {3, 5};
    private static final int[] SET_6 = {4};
    private static final int[] SET_7 = {5};
    private static final int[] SET_8 = {1, 5};
    private static final int[] SET_9 = {0, 1, 5};

    public ModelBovid() {
        this.texWidth = 128;
        this.texHeight = 128;

        this.body_main = new AdvancedModelBox(this, 0, 0);
        this.body_main.setRotationPoint(0F, -1.5F, 0F);
        this.body_main.addBox(-6F, -6.5F, -11F, 12F, 13F, 22F, 0F);
        this.arm_left_1 = new AdvancedModelBox(this, 80, 35);
        this.arm_left_1.setRotationPoint(3F, 4.5F, -8F);
        this.arm_left_1.addBox(-2.5F, 0F, -2.5F, 5F, 10F, 5F, 0F);
        this.arm_left_2 = new AdvancedModelBox(this, 100, 35);
        this.arm_left_2.setRotationPoint(0F, 9F, 0F);
        this.arm_left_2.addBox(-2F, 0F, -2F, 4F, 10F, 4F, 0F);
        this.arm_left_3 = new AdvancedModelBox(this, 48, 75);
        this.arm_left_3.setRotationPoint(0F, 9F, 0F);
        this.arm_left_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.arm_right_1 = new AdvancedModelBox(this, 80, 35);
        this.arm_right_1.mirror = true;
        this.arm_right_1.setRotationPoint(-3F, 4.5F, -8F);
        this.arm_right_1.addBox(-2.5F, 0F, -2.5F, 5F, 10F, 5F, 0F);
        this.arm_right_2 = new AdvancedModelBox(this, 100, 35);
        this.arm_right_2.mirror = true;
        this.arm_right_2.setRotationPoint(0F, 9F, 0F);
        this.arm_right_2.addBox(-2F, 0F, -2F, 4F, 10F, 4F, 0F);
        this.arm_right_3 = new AdvancedModelBox(this, 48, 75);
        this.arm_right_3.mirror = true;
        this.arm_right_3.setRotationPoint(0F, 9F, 0F);
        this.arm_right_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.leg_left_1 = new AdvancedModelBox(this, 0, 35);
        this.leg_left_1.setRotationPoint(3F, 4.5F, 7.5F);
        this.leg_left_1.addBox(-2.5F, 0F, -3F, 5F, 11F, 6F, 0F);
        this.setRotateAngle(leg_left_1, 0.3F, 0F, 0F);
        this.leg_left_2 = new AdvancedModelBox(this, 116, 35);
        this.leg_left_2.setRotationPoint(0F, 10F, 0F);
        this.leg_left_2.addBox(-1.5F, 0F, -1.5F, 3F, 10F, 3F, 0F);
        this.setRotateAngle(leg_left_2, -0.5F, 0F, 0F);
        this.leg_left_3 = new AdvancedModelBox(this, 64, 75);
        this.leg_left_3.setRotationPoint(0F, 9F, 0F);
        this.leg_left_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.setRotateAngle(leg_left_3, 0.3F, 0F, 0F);
        this.leg_right_1 = new AdvancedModelBox(this, 0, 35);
        this.leg_right_1.mirror = true;
        this.leg_right_1.setRotationPoint(-3F, 4.5F, 7.5F);
        this.leg_right_1.addBox(-2.5F, 0F, -3F, 5F, 11F, 6F, 0F);
        this.setRotateAngle(leg_right_1, 0.3F, 0F, 0F);
        this.leg_right_2 = new AdvancedModelBox(this, 116, 35);
        this.leg_right_2.mirror = true;
        this.leg_right_2.setRotationPoint(0F, 10F, 0F);
        this.leg_right_2.addBox(-1.5F, 0F, -1.5F, 3F, 10F, 3F, 0F);
        this.setRotateAngle(leg_right_2, -0.5F, 0F, 0F);
        this.leg_right_3 = new AdvancedModelBox(this, 64, 75);
        this.leg_right_3.mirror = true;
        this.leg_right_3.setRotationPoint(0F, 9F, 0F);
        this.leg_right_3.addBox(-2F, 0F, -2F, 4F, 3F, 4F, 0F);
        this.setRotateAngle(leg_right_3, 0.3F, 0F, 0F);
        this.head_neck = new AdvancedModelBox(this, 22, 35);
        this.head_neck.setRotationPoint(0F, 0F, -10F);
        this.head_neck.addBox(-3.5F, -4.5F, -8F, 7F, 9F, 8F, 0F);
        this.setRotateAngle(head_neck, -0.25F, 0F, 0F);
        this.head_main = new AdvancedModelBox(this, 52, 35);
        this.head_main.setRotationPoint(0F, 0F, -7F);
        this.head_main.addBox(-3F, -4F, -7F, 6F, 8F, 8F, 0F);
        this.setRotateAngle(head_main, 0.5F, 0F, 0F);
        this.head_snout = new AdvancedModelBox(this, 98, 52);
        this.head_snout.setRotationPoint(0F, 1.5F, -7F);
        this.head_snout.addBox(-2.5F, -2.5F, -5F, 5F, 5F, 5F, 0F);
        this.eye_left = new AdvancedModelBox(this, 122, 83);
        this.eye_left.mirror = true;
        this.eye_left.setRotationPoint(3.02F, -2F, -4F);
        this.eye_left.addBox(0F, -0.5F, -1F, 0F, 1F, 2F, 0F);
        this.eye_right = new AdvancedModelBox(this, 122, 83);
        this.eye_right.setRotationPoint(-3.02F, -2F, -4F);
        this.eye_right.addBox(0F, -0.5F, -1F, 0F, 1F, 2F, 0F);
        this.head_ear_left = new AdvancedModelBox(this, 72, 83);
        this.head_ear_left.setRotationPoint(2.5F, -2.5F, 1.5F);
        this.head_ear_left.addBox(0F, -1.5F, 0F, 1F, 3F, 2F, 0F);
        this.setRotateAngle(head_ear_left, 0.2F, 0.5F, 0F);
        this.head_ear_right = new AdvancedModelBox(this, 72, 83);
        this.head_ear_right.mirror = true;
        this.head_ear_right.setRotationPoint(-2.5F, -2.5F, 1.5F);
        this.head_ear_right.addBox(-1F, -1.5F, 0F, 1F, 3F, 2F, 0F);
        this.setRotateAngle(head_ear_right, 0.2F, -0.5F, 0F);
        this.tail = new AdvancedModelBox(this, 78, 52);
        this.tail.setRotationPoint(0F, -4.5F, 10.5F);
        this.tail.addBox(-1F, 0F, -1F, 2F, 9F, 2F, 0F);
        this.setRotateAngle(tail, 0.2F, 0F, 0F);
        this.hl_0_left = new AdvancedModelBox(this, 102, 75);
        this.hl_0_left.setRotationPoint(3F, -2F, 0F);
        this.hl_0_left.addBox(0F, -1.5F, -1.5F, 6F, 3F, 3F, 0F);
        this.setRotateAngle(hl_0_left, 0F, 0F, 0.3F);
        this.hl_0_right = new AdvancedModelBox(this, 102, 75);
        this.hl_0_right.mirror = true;
        this.hl_0_right.setRotationPoint(-3F, -2F, 0F);
        this.hl_0_right.addBox(-6F, -1.5F, -1.5F, 6F, 3F, 3F, 0F);
        this.setRotateAngle(hl_0_right, 0F, 0F, -0.3F);
        this.hl_1_left = new AdvancedModelBox(this, 0, 83);
        this.hl_1_left.setRotationPoint(5.5F, 0F, 0F);
        this.hl_1_left.addBox(0F, -1.5F, -1.5F, 6F, 3F, 3F, 0F);
        this.setRotateAngle(hl_1_left, 0F, 0.9F, 0F);
        this.hl_1_right = new AdvancedModelBox(this, 0, 83);
        this.hl_1_right.mirror = true;
        this.hl_1_right.setRotationPoint(-5.5F, 0F, 0F);
        this.hl_1_right.addBox(-6F, -1.5F, -1.5F, 6F, 3F, 3F, 0F);
        this.setRotateAngle(hl_1_right, 0F, -0.9F, 0F);
        this.hl_2_left = new AdvancedModelBox(this, 18, 83);
        this.hl_2_left.setRotationPoint(5.5F, 0F, 0F);
        this.hl_2_left.addBox(0F, -1.5F, -1.5F, 5F, 3F, 3F, 0F);
        this.setRotateAngle(hl_2_left, 0F, 0.5F, 0.9F);
        this.hl_2_right = new AdvancedModelBox(this, 18, 83);
        this.hl_2_right.mirror = true;
        this.hl_2_right.setRotationPoint(-5.5F, 0F, 0F);
        this.hl_2_right.addBox(-5F, -1.5F, -1.5F, 5F, 3F, 3F, 0F);
        this.setRotateAngle(hl_2_right, 0F, -0.5F, -0.9F);
        this.hlf_0_left = new AdvancedModelBox(this, 94, 83);
        this.hlf_0_left.setRotationPoint(3F, -2F, 0F);
        this.hlf_0_left.addBox(0F, -1F, -1F, 5F, 2F, 2F, 0F);
        this.setRotateAngle(hlf_0_left, 0F, 0F, 0.3F);
        this.hlf_0_right = new AdvancedModelBox(this, 94, 83);
        this.hlf_0_right.mirror = true;
        this.hlf_0_right.setRotationPoint(-3F, -2F, 0F);
        this.hlf_0_right.addBox(-5F, -1F, -1F, 5F, 2F, 2F, 0F);
        this.setRotateAngle(hlf_0_right, 0F, 0F, -0.3F);
        this.hlf_1_left = new AdvancedModelBox(this, 108, 83);
        this.hlf_1_left.setRotationPoint(4.5F, 0F, 0F);
        this.hlf_1_left.addBox(0F, -1F, -1F, 5F, 2F, 2F, 0F);
        this.setRotateAngle(hlf_1_left, 0F, 0.8F, 0.5F);
        this.hlf_1_right = new AdvancedModelBox(this, 108, 83);
        this.hlf_1_right.mirror = true;
        this.hlf_1_right.setRotationPoint(-4.5F, 0F, 0F);
        this.hlf_1_right.addBox(-5F, -1F, -1F, 5F, 2F, 2F, 0F);
        this.setRotateAngle(hlf_1_right, 0F, -0.8F, -0.5F);
        this.boss = new AdvancedModelBox(this, 0, 75);
        this.boss.setRotationPoint(0F, -3.5F, -2F);
        this.boss.addBox(-5F, -2.5F, -2F, 10F, 3F, 5F, 0F);
        this.setRotateAngle(boss, 0.1F, 0F, 0F);
        this.md_0_left = new AdvancedModelBox(this, 0, 65);
        this.md_0_left.setRotationPoint(4.5F, -3F, -1F);
        this.md_0_left.addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F, 0F);
        this.setRotateAngle(md_0_left, 0F, 0F, -0.35F);
        this.md_0_right = new AdvancedModelBox(this, 0, 65);
        this.md_0_right.mirror = true;
        this.md_0_right.setRotationPoint(-4.5F, -3F, -1F);
        this.md_0_right.addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F, 0F);
        this.setRotateAngle(md_0_right, 0F, 0F, 0.35F);
        this.md_1_left = new AdvancedModelBox(this, 72, 65);
        this.md_1_left.setRotationPoint(0F, 6.5F, 0F);
        this.md_1_left.addBox(-1.2F, 0F, -1.2F, 2.4F, 6F, 2.4F, 0F);
        this.setRotateAngle(md_1_left, 0F, 0F, -2.4F);
        this.md_1_right = new AdvancedModelBox(this, 72, 65);
        this.md_1_right.mirror = true;
        this.md_1_right.setRotationPoint(0F, 6.5F, 0F);
        this.md_1_right.addBox(-1.2F, 0F, -1.2F, 2.4F, 6F, 2.4F, 0F);
        this.setRotateAngle(md_1_right, 0F, 0F, 2.4F);
        this.sg_left = new AdvancedModelBox(this, 84, 65);
        this.sg_left.setRotationPoint(2F, -4F, 0F);
        this.sg_left.addBox(-1F, -7F, -1F, 2F, 7F, 2F, 0F);
        this.setRotateAngle(sg_left, -0.15F, 0F, 0.15F);
        this.sg_right = new AdvancedModelBox(this, 84, 65);
        this.sg_right.mirror = true;
        this.sg_right.setRotationPoint(-2F, -4F, 0F);
        this.sg_right.addBox(-1F, -7F, -1F, 2F, 7F, 2F, 0F);
        this.setRotateAngle(sg_right, -0.15F, 0F, -0.15F);
        this.sg_nose = new AdvancedModelBox(this, 12, 65);
        this.sg_nose.setRotationPoint(0F, 3F, -9F);
        this.sg_nose.addBox(-2.5F, -2.5F, -5F, 5F, 5F, 5F, 0F);
        this.setRotateAngle(sg_nose, 0.15F, 0F, 0F);
        this.ib_0_left = new AdvancedModelBox(this, 34, 52);
        this.ib_0_left.setRotationPoint(2.5F, -3.5F, 0F);
        this.ib_0_left.addBox(-1.2F, -9F, -1.2F, 2.4F, 9F, 2.4F, 0F);
        this.setRotateAngle(ib_0_left, -0.5F, 0F, 0.25F);
        this.ib_0_right = new AdvancedModelBox(this, 34, 52);
        this.ib_0_right.mirror = true;
        this.ib_0_right.setRotationPoint(-2.5F, -3.5F, 0F);
        this.ib_0_right.addBox(-1.2F, -9F, -1.2F, 2.4F, 9F, 2.4F, 0F);
        this.setRotateAngle(ib_0_right, -0.5F, 0F, -0.25F);
        this.ib_1_left = new AdvancedModelBox(this, 86, 52);
        this.ib_1_left.setRotationPoint(0F, -8.5F, 0F);
        this.ib_1_left.addBox(-1.2F, -8F, -1.2F, 2.4F, 8F, 2.4F, 0F);
        this.setRotateAngle(ib_1_left, -0.6F, 0F, 0.1F);
        this.ib_1_right = new AdvancedModelBox(this, 86, 52);
        this.ib_1_right.mirror = true;
        this.ib_1_right.setRotationPoint(0F, -8.5F, 0F);
        this.ib_1_right.addBox(-1.2F, -8F, -1.2F, 2.4F, 8F, 2.4F, 0F);
        this.setRotateAngle(ib_1_right, -0.6F, 0F, -0.1F);
        this.ib_2_left = new AdvancedModelBox(this, 80, 75);
        this.ib_2_left.setRotationPoint(0F, -7.5F, 0F);
        this.ib_2_left.addBox(-1F, -5F, -1F, 2F, 5F, 2F, 0F);
        this.setRotateAngle(ib_2_left, -0.7F, 0F, 0F);
        this.ib_2_right = new AdvancedModelBox(this, 80, 75);
        this.ib_2_right.mirror = true;
        this.ib_2_right.setRotationPoint(0F, -7.5F, 0F);
        this.ib_2_right.addBox(-1F, -5F, -1F, 2F, 5F, 2F, 0F);
        this.setRotateAngle(ib_2_right, -0.7F, 0F, 0F);
        this.ibf_0_left = new AdvancedModelBox(this, 34, 83);
        this.ibf_0_left.setRotationPoint(2.5F, -3.5F, 0F);
        this.ibf_0_left.addBox(-1F, -4F, -1F, 2F, 4F, 2F, 0F);
        this.setRotateAngle(ibf_0_left, -0.4F, 0F, 0.2F);
        this.ibf_0_right = new AdvancedModelBox(this, 34, 83);
        this.ibf_0_right.mirror = true;
        this.ibf_0_right.setRotationPoint(-2.5F, -3.5F, 0F);
        this.ibf_0_right.addBox(-1F, -4F, -1F, 2F, 4F, 2F, 0F);
        this.setRotateAngle(ibf_0_right, -0.4F, 0F, -0.2F);
        this.ib_beard = new AdvancedModelBox(this, 42, 83);
        this.ib_beard.setRotationPoint(0F, 3.5F, -7F);
        this.ib_beard.addBox(-1F, 0F, -1F, 2F, 4F, 2F, 0F);
        this.setRotateAngle(ib_beard, 0.2F, 0F, 0F);
        this.bh_0_left = new AdvancedModelBox(this, 32, 65);
        this.bh_0_left.setRotationPoint(2.5F, -2.5F, 0F);
        this.bh_0_left.addBox(0F, -2.2F, -2.2F, 5F, 4.4F, 4.4F, 0F);
        this.setRotateAngle(bh_0_left, 0.3F, 0F, 0.1F);
        this.bh_0_right = new AdvancedModelBox(this, 32, 65);
        this.bh_0_right.mirror = true;
        this.bh_0_right.setRotationPoint(-2.5F, -2.5F, 0F);
        this.bh_0_right.addBox(-5F, -2.2F, -2.2F, 5F, 4.4F, 4.4F, 0F);
        this.setRotateAngle(bh_0_right, 0.3F, 0F, -0.1F);
        this.bh_1_left = new AdvancedModelBox(this, 52, 65);
        this.bh_1_left.setRotationPoint(4.5F, 0F, 0F);
        this.bh_1_left.addBox(0F, -2.2F, -2.2F, 5F, 4.4F, 4.4F, 0F);
        this.setRotateAngle(bh_1_left, 0F, 0F, 1F);
        this.bh_1_right = new AdvancedModelBox(this, 52, 65);
        this.bh_1_right.mirror = true;
        this.bh_1_right.setRotationPoint(-4.5F, 0F, 0F);
        this.bh_1_right.addBox(-5F, -2.2F, -2.2F, 5F, 4.4F, 4.4F, 0F);
        this.setRotateAngle(bh_1_right, 0F, 0F, -1F);
        this.bh_2_left = new AdvancedModelBox(this, 30, 75);
        this.bh_2_left.setRotationPoint(4.5F, 0F, 0F);
        this.bh_2_left.addBox(0F, -2F, -2F, 5F, 4F, 4F, 0F);
        this.setRotateAngle(bh_2_left, 0F, 0F, 1F);
        this.bh_2_right = new AdvancedModelBox(this, 30, 75);
        this.bh_2_right.mirror = true;
        this.bh_2_right.setRotationPoint(-4.5F, 0F, 0F);
        this.bh_2_right.addBox(-5F, -2F, -2F, 5F, 4F, 4F, 0F);
        this.setRotateAngle(bh_2_right, 0F, 0F, -1F);
        this.bh_3_left = new AdvancedModelBox(this, 50, 83);
        this.bh_3_left.setRotationPoint(4.5F, 0F, 0F);
        this.bh_3_left.addBox(0F, -1.5F, -1.5F, 4F, 3F, 3F, 0F);
        this.setRotateAngle(bh_3_left, 0F, 0F, 1F);
        this.bh_3_right = new AdvancedModelBox(this, 50, 83);
        this.bh_3_right.mirror = true;
        this.bh_3_right.setRotationPoint(-4.5F, 0F, 0F);
        this.bh_3_right.addBox(-4F, -1.5F, -1.5F, 4F, 3F, 3F, 0F);
        this.setRotateAngle(bh_3_right, 0F, 0F, -1F);
        this.bhf_0_left = new AdvancedModelBox(this, 64, 83);
        this.bhf_0_left.setRotationPoint(2.5F, -3.5F, 0F);
        this.bhf_0_left.addBox(-1F, -4F, -1F, 2F, 4F, 2F, 0F);
        this.setRotateAngle(bhf_0_left, -0.3F, 0F, 0.2F);
        this.bhf_0_right = new AdvancedModelBox(this, 64, 83);
        this.bhf_0_right.mirror = true;
        this.bhf_0_right.setRotationPoint(-2.5F, -3.5F, 0F);
        this.bhf_0_right.addBox(-1F, -4F, -1F, 2F, 4F, 2F, 0F);
        this.setRotateAngle(bhf_0_right, -0.3F, 0F, -0.2F);
        this.mg_0_left = new AdvancedModelBox(this, 92, 65);
        this.mg_0_left.setRotationPoint(2.2F, -3.5F, 0F);
        this.mg_0_left.addBox(-1.2F, -6F, -1.2F, 2.4F, 6F, 2.4F, 0F);
        this.setRotateAngle(mg_0_left, -0.35F, 0F, 0.1F);
        this.mg_0_right = new AdvancedModelBox(this, 92, 65);
        this.mg_0_right.mirror = true;
        this.mg_0_right.setRotationPoint(-2.2F, -3.5F, 0F);
        this.mg_0_right.addBox(-1.2F, -6F, -1.2F, 2.4F, 6F, 2.4F, 0F);
        this.setRotateAngle(mg_0_right, -0.35F, 0F, -0.1F);
        this.mg_1_left = new AdvancedModelBox(this, 78, 83);
        this.mg_1_left.setRotationPoint(0F, -5.5F, 0F);
        this.mg_1_left.addBox(-1F, -3F, -1F, 2F, 3F, 2F, 0F);
        this.setRotateAngle(mg_1_left, -0.6F, 0F, 0F);
        this.mg_1_right = new AdvancedModelBox(this, 78, 83);
        this.mg_1_right.mirror = true;
        this.mg_1_right.setRotationPoint(0F, -5.5F, 0F);
        this.mg_1_right.addBox(-1F, -3F, -1F, 2F, 3F, 2F, 0F);
        this.setRotateAngle(mg_1_right, -0.6F, 0F, 0F);
        this.mgf_0_left = new AdvancedModelBox(this, 88, 75);
        this.mgf_0_left.setRotationPoint(2.2F, -3.5F, 0F);
        this.mgf_0_left.addBox(-1F, -5F, -1F, 2F, 5F, 2F, 0F);
        this.setRotateAngle(mgf_0_left, -0.35F, 0F, 0.1F);
        this.mgf_0_right = new AdvancedModelBox(this, 88, 75);
        this.mgf_0_right.mirror = true;
        this.mgf_0_right.setRotationPoint(-2.2F, -3.5F, 0F);
        this.mgf_0_right.addBox(-1F, -5F, -1F, 2F, 5F, 2F, 0F);
        this.setRotateAngle(mgf_0_right, -0.35F, 0F, -0.1F);
        this.mgf_1_left = new AdvancedModelBox(this, 86, 83);
        this.mgf_1_left.setRotationPoint(0F, -4.5F, 0F);
        this.mgf_1_left.addBox(-1F, -3F, -1F, 2F, 3F, 2F, 0F);
        this.setRotateAngle(mgf_1_left, -0.6F, 0F, 0F);
        this.mgf_1_right = new AdvancedModelBox(this, 86, 83);
        this.mgf_1_right.mirror = true;
        this.mgf_1_right.setRotationPoint(0F, -4.5F, 0F);
        this.mgf_1_right.addBox(-1F, -3F, -1F, 2F, 3F, 2F, 0F);
        this.setRotateAngle(mgf_1_right, -0.6F, 0F, 0F);
        this.skirt_left = new AdvancedModelBox(this, 68, 0);
        this.skirt_left.setRotationPoint(6.2F, 3F, 0F);
        this.skirt_left.addBox(0F, 0F, -10F, 0F, 14F, 20F, 0F);
        this.skirt_right = new AdvancedModelBox(this, 68, 0);
        this.skirt_right.mirror = true;
        this.skirt_right.setRotationPoint(-6.2F, 3F, 0F);
        this.skirt_right.addBox(0F, 0F, -10F, 0F, 14F, 20F, 0F);
        this.mane = new AdvancedModelBox(this, 46, 52);
        this.mane.setRotationPoint(0F, -3F, 0F);
        this.mane.addBox(-3.5F, -3.5F, -8F, 7F, 3F, 9F, 0F);
        this.hump = new AdvancedModelBox(this, 0, 52);
        this.hump.setRotationPoint(0F, -6.5F, -5F);
        this.hump.addBox(-4F, -3F, -5F, 8F, 4F, 9F, 0F);
        this.ear_sg_left = new AdvancedModelBox(this, 96, 75);
        this.ear_sg_left.setRotationPoint(2.8F, -2.5F, 1.5F);
        this.ear_sg_left.addBox(0F, -2.5F, 0F, 1F, 5F, 2F, 0F);
        this.setRotateAngle(ear_sg_left, 0.1F, 0.5F, 0F);
        this.ear_sg_right = new AdvancedModelBox(this, 96, 75);
        this.ear_sg_right.mirror = true;
        this.ear_sg_right.setRotationPoint(-2.8F, -2.5F, 1.5F);
        this.ear_sg_right.addBox(-1F, -2.5F, 0F, 1F, 5F, 2F, 0F);
        this.setRotateAngle(ear_sg_right, 0.1F, -0.5F, 0F);
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
        this.head_main.addChild(this.hl_0_left);
        this.head_main.addChild(this.hl_0_right);
        this.hl_0_left.addChild(this.hl_1_left);
        this.hl_0_right.addChild(this.hl_1_right);
        this.hl_1_left.addChild(this.hl_2_left);
        this.hl_1_right.addChild(this.hl_2_right);
        this.head_main.addChild(this.hlf_0_left);
        this.head_main.addChild(this.hlf_0_right);
        this.hlf_0_left.addChild(this.hlf_1_left);
        this.hlf_0_right.addChild(this.hlf_1_right);
        this.head_main.addChild(this.boss);
        this.head_main.addChild(this.md_0_left);
        this.head_main.addChild(this.md_0_right);
        this.md_0_left.addChild(this.md_1_left);
        this.md_0_right.addChild(this.md_1_right);
        this.head_main.addChild(this.sg_left);
        this.head_main.addChild(this.sg_right);
        this.head_main.addChild(this.sg_nose);
        this.head_main.addChild(this.ib_0_left);
        this.head_main.addChild(this.ib_0_right);
        this.ib_0_left.addChild(this.ib_1_left);
        this.ib_0_right.addChild(this.ib_1_right);
        this.ib_1_left.addChild(this.ib_2_left);
        this.ib_1_right.addChild(this.ib_2_right);
        this.head_main.addChild(this.ibf_0_left);
        this.head_main.addChild(this.ibf_0_right);
        this.head_main.addChild(this.ib_beard);
        this.head_main.addChild(this.bh_0_left);
        this.head_main.addChild(this.bh_0_right);
        this.bh_0_left.addChild(this.bh_1_left);
        this.bh_0_right.addChild(this.bh_1_right);
        this.bh_1_left.addChild(this.bh_2_left);
        this.bh_1_right.addChild(this.bh_2_right);
        this.bh_2_left.addChild(this.bh_3_left);
        this.bh_2_right.addChild(this.bh_3_right);
        this.head_main.addChild(this.bhf_0_left);
        this.head_main.addChild(this.bhf_0_right);
        this.head_main.addChild(this.mg_0_left);
        this.head_main.addChild(this.mg_0_right);
        this.mg_0_left.addChild(this.mg_1_left);
        this.mg_0_right.addChild(this.mg_1_right);
        this.head_main.addChild(this.mgf_0_left);
        this.head_main.addChild(this.mgf_0_right);
        this.mgf_0_left.addChild(this.mgf_1_left);
        this.mgf_0_right.addChild(this.mgf_1_right);
        this.body_main.addChild(this.skirt_left);
        this.body_main.addChild(this.skirt_right);
        this.head_neck.addChild(this.mane);
        this.body_main.addChild(this.hump);
        this.head_main.addChild(this.ear_sg_left);
        this.head_main.addChild(this.ear_sg_right);
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
            hl_0_left,
            hl_0_right,
            hl_1_left,
            hl_1_right,
            hl_2_left,
            hl_2_right,
            hlf_0_left,
            hlf_0_right,
            hlf_1_left,
            hlf_1_right,
            boss,
            md_0_left,
            md_0_right,
            md_1_left,
            md_1_right,
            sg_left,
            sg_right,
            sg_nose,
            ib_0_left,
            ib_0_right,
            ib_1_left,
            ib_1_right,
            ib_2_left,
            ib_2_right,
            ibf_0_left,
            ibf_0_right,
            ib_beard,
            bh_0_left,
            bh_0_right,
            bh_1_left,
            bh_1_right,
            bh_2_left,
            bh_2_right,
            bh_3_left,
            bh_3_right,
            bhf_0_left,
            bhf_0_right,
            mg_0_left,
            mg_0_right,
            mg_1_left,
            mg_1_right,
            mgf_0_left,
            mgf_0_right,
            mgf_1_left,
            mgf_1_right,
            skirt_left,
            skirt_right,
            mane,
            hump,
            ear_sg_left,
            ear_sg_right
        );
    }

    private static boolean in(int[] set, int v) {
        for (int s : set) {
            if (s == v) return true;
        }
        return false;
    }

    public void setupAnim(EntityBovid mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetToDefaultPose();
        this.head_ear_left.showModel = in(SET_0, mob.getVariant());
        this.head_ear_right.showModel = in(SET_0, mob.getVariant());
        this.hl_0_left.showModel = in(SET_1, mob.getVariant()) && mob.isMale();
        this.hl_0_right.showModel = in(SET_1, mob.getVariant()) && mob.isMale();
        this.hl_1_left.showModel = in(SET_1, mob.getVariant()) && mob.isMale();
        this.hl_1_right.showModel = in(SET_1, mob.getVariant()) && mob.isMale();
        this.hl_2_left.showModel = in(SET_1, mob.getVariant()) && mob.isMale();
        this.hl_2_right.showModel = in(SET_1, mob.getVariant()) && mob.isMale();
        this.hlf_0_left.showModel = in(SET_1, mob.getVariant()) && !mob.isMale();
        this.hlf_0_right.showModel = in(SET_1, mob.getVariant()) && !mob.isMale();
        this.hlf_1_left.showModel = in(SET_1, mob.getVariant()) && !mob.isMale();
        this.hlf_1_right.showModel = in(SET_1, mob.getVariant()) && !mob.isMale();
        this.boss.showModel = in(SET_2, mob.getVariant());
        this.md_0_left.showModel = in(SET_2, mob.getVariant());
        this.md_0_right.showModel = in(SET_2, mob.getVariant());
        this.md_1_left.showModel = in(SET_2, mob.getVariant());
        this.md_1_right.showModel = in(SET_2, mob.getVariant());
        this.sg_left.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.sg_right.showModel = in(SET_3, mob.getVariant()) && mob.isMale();
        this.sg_nose.showModel = in(SET_3, mob.getVariant());
        this.ib_0_left.showModel = in(SET_4, mob.getVariant()) && mob.isMale();
        this.ib_0_right.showModel = in(SET_4, mob.getVariant()) && mob.isMale();
        this.ib_1_left.showModel = in(SET_4, mob.getVariant()) && mob.isMale();
        this.ib_1_right.showModel = in(SET_4, mob.getVariant()) && mob.isMale();
        this.ib_2_left.showModel = in(SET_4, mob.getVariant()) && mob.isMale();
        this.ib_2_right.showModel = in(SET_4, mob.getVariant()) && mob.isMale();
        this.ibf_0_left.showModel = in(SET_4, mob.getVariant()) && !mob.isMale();
        this.ibf_0_right.showModel = in(SET_4, mob.getVariant()) && !mob.isMale();
        this.ib_beard.showModel = in(SET_5, mob.getVariant()) && mob.isMale();
        this.bh_0_left.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bh_0_right.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bh_1_left.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bh_1_right.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bh_2_left.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bh_2_right.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bh_3_left.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bh_3_right.showModel = in(SET_6, mob.getVariant()) && mob.isMale();
        this.bhf_0_left.showModel = in(SET_6, mob.getVariant()) && !mob.isMale();
        this.bhf_0_right.showModel = in(SET_6, mob.getVariant()) && !mob.isMale();
        this.mg_0_left.showModel = in(SET_7, mob.getVariant()) && mob.isMale();
        this.mg_0_right.showModel = in(SET_7, mob.getVariant()) && mob.isMale();
        this.mg_1_left.showModel = in(SET_7, mob.getVariant()) && mob.isMale();
        this.mg_1_right.showModel = in(SET_7, mob.getVariant()) && mob.isMale();
        this.mgf_0_left.showModel = in(SET_7, mob.getVariant()) && !mob.isMale();
        this.mgf_0_right.showModel = in(SET_7, mob.getVariant()) && !mob.isMale();
        this.mgf_1_left.showModel = in(SET_7, mob.getVariant()) && !mob.isMale();
        this.mgf_1_right.showModel = in(SET_7, mob.getVariant()) && !mob.isMale();
        this.skirt_left.showModel = in(SET_2, mob.getVariant());
        this.skirt_right.showModel = in(SET_2, mob.getVariant());
        this.mane.showModel = in(SET_8, mob.getVariant());
        this.hump.showModel = in(SET_9, mob.getVariant());
        this.ear_sg_left.showModel = in(SET_3, mob.getVariant());
        this.ear_sg_right.showModel = in(SET_3, mob.getVariant());
        float globalSpeed = 1.5f;
        float globalDegree = 1f;
        float f = limbSwing / 2;
        limbSwingAmount = Math.min(0.4F, limbSwingAmount);
        float gait = 1F;
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
            this.progressPosition(body_main, rest, 0.0F, 13F, 0.0F, 40);
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
