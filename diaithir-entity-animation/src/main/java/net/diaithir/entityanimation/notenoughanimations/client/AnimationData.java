package net.diaithir.entityanimation.notenoughanimations.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.core.component.DataComponents;

final class AnimationData {
    static final Map<UUID, PoseState> STATES = new WeakHashMap<>();
    static final Map<Identifier, PoseType> OVERRIDES = new HashMap<>();
    static final Map<UUID, UUID> KEYS = new HashMap<>();

    private AnimationData() {}

    static PoseState state(UUID id) {
        UUID key = KEYS.computeIfAbsent(id, u -> u);
        return STATES.computeIfAbsent(key, k -> new PoseState());
    }

    static void clear() {
        STATES.clear();
        KEYS.clear();
    }

    static void rebuildOverrides() {
        OVERRIDES.clear();
        for (String s : AnimationConfig.ITEM_OVERRIDES.get()) {
            int i = s.indexOf('=');
            if (i < 0) continue;
            Identifier id = Identifier.tryParse(s.substring(0, i).trim());
            PoseType t = PoseType.parse(s.substring(i + 1));
            if (id != null && t != null) OVERRIDES.put(id, t);
        }
    }

    static PoseType classify(ItemStack stack) {
        if (stack.isEmpty()) return PoseType.NONE;
        PoseType o = OVERRIDES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (o != null) return o;
        ItemUseAnimation ua = stack.getUseAnimation();
        if (ua == ItemUseAnimation.BLOCK) return PoseType.SHIELD;
        if (ua == ItemUseAnimation.EAT || ua == ItemUseAnimation.DRINK) return PoseType.FOOD;
        if (stack.getItem() instanceof CrossbowItem) return PoseType.TWOHANDED;
        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof TridentItem
                || ua == ItemUseAnimation.BOW || ua == ItemUseAnimation.SPEAR || ua == ItemUseAnimation.SPYGLASS) return PoseType.RANGED;
        if (stack.getItem() instanceof BlockItem) return PoseType.BLOCK;
        if (stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON)) return PoseType.TOOL;
        return PoseType.NONE;
    }

    static boolean bypass(AbstractClientPlayer p) {
        Pose pose = p.getPose();
        return pose == Pose.FALL_FLYING || pose == Pose.SWIMMING || pose == Pose.SLEEPING
                || pose == Pose.DYING || p.isDeadOrDying() || p.isSleeping() || p.isFallFlying()
                || p.isSwimming() || p.isVisuallyCrawling();
    }

    static boolean isFirstPerson(AbstractClientPlayer p) {
        Minecraft mc = Minecraft.getInstance();
        return p == mc.player && mc.options.getCameraType().isFirstPerson();
    }

    static boolean inBoat(AbstractClientPlayer p) {
        return p.getVehicle() instanceof AbstractBoat;
    }

    static void tick(AbstractClientPlayer p) {
        PoseState s = state(p.getUUID());
        double dx = p.getX() - p.xo, dz = p.getZ() - p.zo;
        float spd = (float) Math.sqrt(dx * dx + dz * dz) * 4.0F;
        s.speed += (Mth.clamp(spd, 0, 1.5F) - s.speed) * 0.4F;

        float k = AnimationConfig.SMOOTHING_SPEED.get().floatValue();
        float intensity = AnimationConfig.INTENSITY.get().floatValue();
        boolean ok = !bypass(p);

        float sneakT = ok && AnimationConfig.SNEAK_ANIMATION.get() && p.isCrouching() ? 1F : 0F;
        s.sneak += (sneakT - s.sneak) * k;

        boolean riding = ok && AnimationConfig.RIDING_ANIMATION.get() && p.isPassenger();
        float splayT = riding && !inBoat(p) ? 1F : 0F;
        s.legSplay += (splayT - s.legSplay) * k;

        float rx = 0, lx = 0, ry = 0, ly = 0, rz = 0, lz = 0;
        float chewT = 0;
        boolean right = p.getMainArm() == HumanoidArm.RIGHT;

        if (ok && AnimationConfig.HELD_ITEM_POSES.get() && s.speed < 0.05F) {
            for (InteractionHand h : InteractionHand.values()) {
                boolean isRight = (h == InteractionHand.MAIN_HAND) == right;
                float[] d = heldDelta(classify(p.getItemInHand(h)));
                if (isRight) { rx += d[0]; rz += d[1]; } else { lx += d[0]; lz -= d[1]; }
                if (d[2] != 0) { rx += d[2]; lx += d[2]; }
            }
        }

        if (ok && AnimationConfig.USE_ITEM_ANIMATIONS.get() && p.isUsingItem()) {
            ItemStack use = p.getUseItem();
            boolean isRight = (p.getUsedItemHand() == InteractionHand.MAIN_HAND) == right;
            ItemUseAnimation ua = use.getUseAnimation();
            float ax = 0, ay = 0;
            if (ua == ItemUseAnimation.EAT || ua == ItemUseAnimation.DRINK) {
                ax = -1.1F; ay = isRight ? -0.3F : 0.3F;
                chewT = 1F;
            } else if (ua == ItemUseAnimation.BLOCK) {
                ax = -0.5F; ay = isRight ? 0.4F : -0.4F;
            } else if (ua == ItemUseAnimation.BOW || ua == ItemUseAnimation.SPEAR || ua == ItemUseAnimation.CROSSBOW) {
                ax = -0.2F;
            } else if (ua == ItemUseAnimation.SPYGLASS) {
                ax = -0.9F; ay = isRight ? -0.2F : 0.2F;
            }
            if (isRight) { rx += ax; ry += ay; } else { lx += ax; ly += ay; }
        }

        if (s.sneak > 0.01F) {
            rx += 0.15F * s.sneak; lx += 0.15F * s.sneak;
            rz += 0.12F * s.sneak; lz -= 0.12F * s.sneak;
        }

        if (riding && p.getVehicle() instanceof AbstractBoat) {
            rx -= 0.25F; lx -= 0.25F;
        } else if (riding && p.getControllingPassenger() == p) {
            rx -= 0.3F; lx -= 0.3F;
        }

        rx *= intensity; lx *= intensity; ry *= intensity; ly *= intensity; rz *= intensity; lz *= intensity;
        s.rightArmX += (rx - s.rightArmX) * k;
        s.leftArmX += (lx - s.leftArmX) * k;
        s.rightArmY += (ry - s.rightArmY) * k;
        s.leftArmY += (ly - s.leftArmY) * k;
        s.rightArmZ += (rz - s.rightArmZ) * k;
        s.leftArmZ += (lz - s.leftArmZ) * k;
        s.chew += (chewT - s.chew) * k;

        float yaw = p.yBodyRot;
        float diff = Mth.wrapDegrees(yaw - s.prevBodyYaw);
        s.prevBodyYaw = yaw;
        if (!s.init) { s.init = true; diff = 0; s.lagYaw = 0; }
        float lagT = ok && AnimationConfig.HEAD_LAG.get() ? Mth.clamp(-diff * 0.02F, -0.35F, 0.35F) : 0F;
        s.lagYaw += (lagT - s.lagYaw) * 0.25F;
    }

    private static float[] heldDelta(PoseType t) {
        return switch (t) {
            case TOOL -> new float[] {-0.08F, 0.05F, 0F};
            case BLOCK -> new float[] {-0.2F, 0.08F, 0F};
            case RANGED -> new float[] {-0.15F, 0F, 0F};
            case FOOD -> new float[] {-0.1F, 0F, 0F};
            case SHIELD -> new float[] {-0.15F, -0.1F, 0F};
            case TWOHANDED -> new float[] {-0.1F, 0F, -0.45F};
            default -> new float[] {0F, 0F, 0F};
        };
    }
}
