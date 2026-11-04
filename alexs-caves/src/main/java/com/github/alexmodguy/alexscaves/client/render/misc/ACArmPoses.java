package com.github.alexmodguy.alexscaves.client.render.misc;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.NuclearBombEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.GumWormSegmentEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.SubterranodonEntity;
import com.github.alexmodguy.alexscaves.server.item.CandyCaneHookItem;
import com.github.alexmodguy.alexscaves.server.item.GalenaGauntletItem;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import com.github.alexmodguy.alexscaves.server.item.ResistorShieldItem;
import com.github.alexmodguy.alexscaves.server.item.ShotGumItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IArmPoseTransformer;

/**
 * Custom third-person arm poses (replaces upstream's EventPosePlayerHand). The poses are NeoForge ArmPose enum extensions
 * (enumextensions.json); the render state modifier picks one per arm and stores the numbers the transform needs.
 */
public class ACArmPoses {
    public static final ContextKey<Data> KEY = new ContextKey<>(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "arm_poses"));

    public enum Kind {RAYGUN, GAUNTLET, SHIELD, SHOTGUM, HOOK, SUBTERRANODON, BOMB, SUGAR}

    public static class Data {
        public final float[] raygun = new float[2];
        public final float[] gauntlet = new float[2];
        public final float[] shield = new float[2];
        public boolean shotgumRightMain;
        public float flight;
        public float wiggleRight;
        public float wiggleLeft;
        public float speedModifier = 0.35F;
        public float walkPos;
        public float walkSpeed;
        public float headXRot;
        public float headYRot;
    }

    private static HumanoidModel.ArmPose pose(Kind kind) {
        return HumanoidModel.ArmPose.valueOf("ALEXSCAVES_" + kind.name());
    }

    /** Parameter providers referenced by enumextensions.json: (twoHanded, affectsOffhandPose, transformer). */
    private static Object param(Kind kind, boolean twoHanded, boolean affectsOffhand, int idx, Class<?> type) {
        // no type.cast: the requested type is boolean.class for the first two, and Class.cast rejects the boxed Boolean for a primitive
        return (Object) (switch (idx) {
            case 0 -> twoHanded;
            case 1 -> affectsOffhand;
            case 2 -> (IArmPoseTransformer) (model, state, arm) -> transform(kind, model, state, arm);
            default -> throw new IllegalArgumentException("Unexpected parameter index: " + idx);
        });
    }

    public static Object raygun(int idx, Class<?> type) { return param(Kind.RAYGUN, false, false, idx, type); }
    public static Object gauntlet(int idx, Class<?> type) { return param(Kind.GAUNTLET, false, false, idx, type); }
    public static Object shield(int idx, Class<?> type) { return param(Kind.SHIELD, false, false, idx, type); }
    public static Object shotgum(int idx, Class<?> type) { return param(Kind.SHOTGUM, true, true, idx, type); }
    public static Object hook(int idx, Class<?> type) { return param(Kind.HOOK, true, true, idx, type); }
    public static Object subterranodon(int idx, Class<?> type) { return param(Kind.SUBTERRANODON, true, true, idx, type); }
    public static Object bomb(int idx, Class<?> type) { return param(Kind.BOMB, true, true, idx, type); }
    public static Object sugar(int idx, Class<?> type) { return param(Kind.SUGAR, true, true, idx, type); }

    private static float rad(float deg) {
        return (float) Math.toRadians(deg);
    }

    private static void transform(Kind kind, HumanoidModel<?> m, HumanoidRenderState state, HumanoidArm armSide) {
        Data d = state.getRenderData(KEY);
        if (d == null) return;
        boolean right = armSide == HumanoidArm.RIGHT;
        int i = right ? 0 : 1;
        var arm = right ? m.rightArm : m.leftArm;
        switch (kind) {
            case RAYGUN -> {
                float p = Math.min(5F, d.raygun[i]) / 5F;
                arm.xRot = (m.head.xRot - rad(80F)) * p;
                arm.yRot = m.head.yRot * p;
                arm.zRot = 0;
            }
            case GAUNTLET -> {
                float p = Math.min(5F, d.gauntlet[i]) / 5F;
                arm.xRot = (m.head.xRot - rad(80F)) * p;
                arm.yRot = m.head.yRot * p;
            }
            case SHIELD -> {
                float p = Math.min(10F, d.shield[i]) / 10F;
                float turn = Math.min(p * 4F, 1F);
                float up = (float) Math.sin(p * Math.PI);
                float tilt = state.isCrouching ? 120F : 80F;
                arm.xRot = -rad(tilt) - rad(80F) * up;
                arm.yRot = (right ? -1F : 1F) * rad(20F) * turn;
            }
            case SHOTGUM -> {
                boolean rightMain = d.shotgumRightMain;
                var main = rightMain ? m.rightArm : m.leftArm;
                var off = rightMain ? m.leftArm : m.rightArm;
                main.xRot = m.head.xRot - rad(70F);
                main.yRot = m.head.yRot;
                main.zRot = 0;
                off.xRot = m.head.xRot - rad(70F);
                off.yRot = m.head.yRot + (rightMain ? rad(40F) : rad(-40F));
                off.zRot = rightMain ? rad(20F) : rad(-20F);
            }
            case HOOK -> {
                m.rightArm.xRot = rad(-100F + d.wiggleRight);
                m.leftArm.xRot = rad(-100F + d.wiggleLeft);
                m.rightArm.yRot = rad(20F);
                m.leftArm.yRot = rad(-20F);
                m.rightLeg.xRot = rad(-20F);
                m.leftLeg.xRot = rad(20F);
            }
            case SUBTERRANODON -> {
                m.leftArm.xRot = -rad(180F) * d.flight;
                m.leftArm.zRot = rad(-10F) * d.flight;
                m.rightArm.xRot = -rad(180F) * d.flight;
                m.rightArm.zRot = rad(10F) * d.flight;
            }
            case BOMB -> {
                float age = state.ageInTicks;
                m.rightArm.xRot = rad(-170F);
                m.rightArm.yRot = rad(100F) + (float) Math.cos(age * 0.35F) * rad(20F);
                m.rightArm.zRot = (float) Math.sin(age * 0.35F) * rad(50F) - rad(70F);
                m.leftArm.yRot = rad(30F);
            }
            case SUGAR -> {
                float sp = d.speedModifier;
                float wp = d.walkPos;
                float ws = d.walkSpeed;
                m.rightArm.xRot = Mth.cos(wp * sp + (float) Math.PI * 0.5F) * 2.0F * ws * 0.5F;
                m.leftArm.xRot = Mth.cos(wp * sp) * 2.0F * ws * 0.5F;
                m.rightArm.zRot = (Mth.sin(wp * -sp + (float) Math.PI * 0.5F) + 2.5F) * 1.5F * ws * 0.5F;
                m.leftArm.zRot = (Mth.sin(wp * -sp) - 2.5F) * 1.5F * ws * 0.5F;
                m.head.xRot = d.headXRot * ((float) Math.PI / 180F) + Mth.cos(wp * sp + (float) Math.PI) * ws * 0.5F;
                m.head.yRot = d.headYRot * ((float) Math.PI / 180F) + Mth.sin(wp * sp + (float) Math.PI) * ws * 0.5F;
                m.leftLeg.xRot = Mth.cos(wp * sp + (float) Math.PI) * 4.0F * ws * 0.5F;
                m.rightLeg.xRot = Mth.cos(wp * sp) * 4.0F * ws * 0.5F;
            }
        }
    }

    /** Called from the avatar render state modifier after vanilla extraction. */
    public static void extract(LivingEntity living, LivingEntityRenderState st) {
        if (!(st instanceof HumanoidRenderState state) || !(living instanceof Player player)) return;
        float pt = state.partialTick;
        Data d = new Data();
        boolean any = false;
        boolean mainRight = player.getMainArm() == HumanoidArm.RIGHT;
        HumanoidModel.ArmPose rightPose = null;
        HumanoidModel.ArmPose leftPose = null;
        // both-arm and body poses first (they override the per-arm ones, like upstream)
        HumanoidModel.ArmPose both = null;
        if (player.isPassenger() && player.getVehicle() instanceof SubterranodonEntity sub) {
            d.flight = sub.getFlyProgress(pt) - sub.getHoverProgress(pt);
            if (d.flight > 0F) both = pose(Kind.SUBTERRANODON);
        }
        if (player.getVehicle() instanceof NuclearBombEntity) both = pose(Kind.BOMB);
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (both == null) {
            for (int h = 0; h < 2; h++) {
                ItemStack stack = h == 0 ? main : off;
                boolean rightArm = (h == 0) == mainRight;
                int idx = rightArm ? 0 : 1;
                HumanoidModel.ArmPose p = null;
                if (stack.getItem() instanceof ResistorShieldItem) {
                    d.shield[idx] = Math.max(d.shield[idx], ResistorShieldItem.getLerpedUseTime(stack, pt));
                    if (d.shield[idx] > 0F) p = pose(Kind.SHIELD);
                } else if (stack.getItem() instanceof GalenaGauntletItem) {
                    d.gauntlet[idx] = Math.max(d.gauntlet[idx], GalenaGauntletItem.getLerpedUseTime(stack, pt));
                    if (d.gauntlet[idx] > 0F) p = pose(Kind.GAUNTLET);
                } else if (stack.getItem() instanceof RaygunItem) {
                    d.raygun[idx] = Math.max(d.raygun[idx], RaygunItem.getLerpedUseTime(stack, pt));
                    if (d.raygun[idx] > 0F) p = pose(Kind.RAYGUN);
                }
                if (p != null) {
                    if (rightArm) rightPose = p; else leftPose = p;
                }
            }
            if (main.getItem() instanceof ShotGumItem && ShotGumItem.shouldBeHeldUpright(main)) {
                d.shotgumRightMain = mainRight;
                both = pose(Kind.SHOTGUM);
            } else if (off.getItem() instanceof ShotGumItem && ShotGumItem.shouldBeHeldUpright(off)) {
                d.shotgumRightMain = !mainRight;
                both = pose(Kind.SHOTGUM);
            }
            if (main.getItem() instanceof CandyCaneHookItem && CandyCaneHookItem.isActive(main)
                    && off.getItem() instanceof CandyCaneHookItem && CandyCaneHookItem.isActive(off)
                    && player.getVehicle() instanceof GumWormSegmentEntity) {
                float t = player.tickCount + pt;
                d.wiggleRight = -Math.min(player.xxa, 0F) * (float) Math.sin(t) * 25;
                d.wiggleLeft = Math.max(player.xxa, 0F) * (float) Math.sin(t) * 25;
                both = pose(Kind.HOOK);
            }
            if (both == null && rightPose == null && leftPose == null && player.hasEffect(ACEffectRegistry.SUGAR_RUSH)
                    && !AlexsCaves.PROXY.isFirstPersonPlayer(player)) {
                float speed = 0.35F;
                d.speedModifier = speed;
                d.walkPos = player.walkAnimation.position(pt);
                d.walkSpeed = player.walkAnimation.speed(pt);
                d.headXRot = player.getViewXRot(pt);
                d.headYRot = Mth.rotLerp(pt, player.yHeadRotO, player.yHeadRot) - Mth.rotLerp(pt, player.yBodyRotO, player.yBodyRot);
                both = pose(Kind.SUGAR);
            }
        }
        if (both != null) {
            state.rightArmPose = both;
            state.leftArmPose = both;
            any = true;
        } else {
            if (rightPose != null) { state.rightArmPose = rightPose; any = true; }
            if (leftPose != null) { state.leftArmPose = leftPose; any = true; }
        }
        if (any) state.setRenderData(KEY, d);
    }
}
