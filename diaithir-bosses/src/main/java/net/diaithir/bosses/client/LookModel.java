package net.diaithir.bosses.client;

import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * A planet boss's own model, built from its look file. Part names carry the animation: head*, leg_n, armN_l/r, wing_l/r, tail_n,
 * seg_n (serpent body), neck*, orbit_n and halo (floating), robe/body_n (hovering bodies).
 */
public final class LookModel extends EntityModel<PlanetBossRenderState> {
    private final List<ModelPart> parts;
    private final List<String> names;

    LookModel(ModelPart root, List<String> names) {
        super(root);
        this.names = names;
        this.parts = names.stream().map(root::getChild).toList();
    }

    @Override
    public void setupAnim(PlanetBossRenderState s) {
        super.setupAnim(s);
        float t = s.ageInTicks, walk = s.walkAnimationPos, amp = Math.min(1.0F, s.walkAnimationSpeed * 2.0F);
        for (int i = 0; i < parts.size(); i++) {
            String n = names.get(i);
            ModelPart p = parts.get(i);
            int k = idx(n);
            if (n.startsWith("head")) {
                p.yRot = s.yRot * Mth.DEG_TO_RAD;
                p.xRot = s.xRot * Mth.DEG_TO_RAD;
            } else if (n.startsWith("leg_")) {
                float ph = (k % 2 == 0 ? 0 : Mth.PI) + ((k / 2) % 2 == 0 ? 0 : Mth.PI);
                p.xRot = Mth.cos(walk * 0.6662F + ph) * 1.2F * amp;
            } else if (n.startsWith("arm")) {
                p.xRot = Mth.cos(walk * 0.6662F + (n.endsWith("_l") ? Mth.PI : 0)) * 0.9F * amp;
                p.zRot = (n.endsWith("_l") ? 1 : -1) * (0.04F + Mth.sin(t * 0.09F) * 0.04F);
            } else if (n.startsWith("wing")) {
                p.zRot = (n.endsWith("_l") ? -1 : 1) * Mth.sin(t * (0.18F + 0.15F * amp)) * 0.45F;
            } else if (n.startsWith("tail_")) {
                p.yRot = Mth.sin(t * 0.1F - k * 0.6F) * 0.2F * (1 + k * 0.4F);
            } else if (n.startsWith("seg_")) {
                p.yRot = Mth.sin(t * 0.12F - k * 0.5F) * 0.12F;
            } else if (n.startsWith("neck")) {
                p.xRot = Mth.sin(t * 0.07F + k) * 0.05F;
            } else if (n.startsWith("orbit_")) {
                p.yRot = t * 0.05F + k;
                p.y += Mth.sin(t * 0.1F + k * 1.7F) * 1.5F;
            } else if (n.equals("halo")) {
                p.yRot = t * 0.04F;
            } else if (n.startsWith("body_") || n.equals("robe")) {
                p.y += Mth.sin(t * 0.08F + k) * 0.8F;
            }
            rise(n, p, s);
            attack(n, p, k, s);
            dying(n, p, s);
        }
    }

    /** Waking at the altar: the body unfolds over the first two seconds. */
    private static void rise(String n, ModelPart p, PlanetBossRenderState s) {
        if (s.ageInTicks >= 40) return;
        float k = 1.0F - s.ageInTicks / 40.0F;
        if (n.startsWith("head")) p.xRot += 0.9F * k;
        else if (n.startsWith("arm")) p.xRot += 0.6F * k;
        else if (n.startsWith("body_") || n.equals("robe") || n.startsWith("orbit_")) p.y += 6.0F * k;
    }

    /** Pose overlay per attack type. Phase 1 is the wind-up (telegraph), phase 2 the strike. Anything not listed keeps its base motion. */
    private static void attack(String n, ModelPart p, int k, PlanetBossRenderState s) {
        if (s.animPhase == 0 || s.anim.isEmpty()) return;
        boolean wind = s.animPhase == 1;
        float u = Mth.clamp(s.animT / (wind ? 20.0F : 8.0F), 0.0F, 1.0F);
        boolean arm = n.startsWith("arm"), head = n.startsWith("head"), leg = n.startsWith("leg_");
        boolean left = n.endsWith("_l");
        switch (s.anim) {
            case "slam", "wave" -> {
                if (arm) p.xRot = wind ? -2.6F * u : -2.6F + 3.4F * u;
                else if (head) p.xRot += wind ? -0.4F * u : 0.5F * u;
                else if (n.startsWith("body")) p.xRot += wind ? -0.2F * u : 0.35F * u;
            }
            case "leap" -> {
                if (leg) p.xRot = wind ? 0.9F * u : -0.6F;
                else if (arm) p.xRot = wind ? 0.8F * u : -1.8F;
                else if (n.startsWith("body")) p.y += wind ? 2.0F * u : -3.0F;
            }
            case "charge" -> {
                if (head) p.xRot += 0.6F * u;
                else if (arm) p.xRot = wind ? -0.5F * u : 1.2F;
                else if (leg) p.xRot *= wind ? 1.0F : 1.6F;
            }
            case "roar" -> {
                if (head) { p.xRot = -0.8F * u; p.yRot += Mth.sin(s.ageInTicks * 1.6F) * 0.08F * u; }
                else if (arm) { p.xRot = -0.4F * u; p.zRot = (left ? 1 : -1) * 1.1F * u; }
            }
            case "gravity_well" -> {
                if (arm) { p.xRot = -1.5F * u; p.zRot = (left ? 1 : -1) * (0.3F + 0.15F * Mth.sin(s.ageInTicks * 0.5F)); }
                else if (n.startsWith("orbit_")) p.yRot = s.ageInTicks * (wind ? 0.2F : 0.5F) + k;
                else if (head) p.xRot -= 0.3F * u;
            }
            case "rewind" -> {
                if (arm) p.xRot = (wind ? 1.0F : 1.6F) * u - 0.3F;
                else if (n.startsWith("orbit_")) p.yRot = -s.ageInTicks * 0.45F + k;
                else if (n.equals("halo")) p.yRot = -s.ageInTicks * 0.3F;
                else if (head) p.yRot += Mth.sin(s.ageInTicks * 0.9F) * 0.2F * u;
            }
            case "lightning", "bolt", "cloud", "summon" -> {
                if (arm) { p.xRot = (wind ? -2.8F : -1.0F) * u; p.zRot = (left ? 1 : -1) * 0.3F; }
                else if (head) p.xRot -= 0.4F * u;
            }
            case "heal" -> {
                if (arm) { p.xRot = -0.9F * u; p.zRot = (left ? -1 : 1) * 0.5F * u; }
                else if (n.startsWith("body_") || n.equals("robe")) p.y += Mth.sin(s.ageInTicks * 0.3F) * 0.8F;
            }
            default -> {
                if (arm) p.xRot = wind ? -1.4F * u : 0.6F;
            }
        }
    }

    /** Death: the limbs go slack as the vanilla death tilt plays. */
    private static void dying(String n, ModelPart p, PlanetBossRenderState s) {
        if (s.deathTime <= 0) return;
        float k = Math.min(1.0F, s.deathTime / 20.0F);
        if (n.startsWith("arm")) { p.xRot = 0.0F; p.zRot = (n.endsWith("_l") ? 1 : -1) * 0.9F * k; }
        else if (n.startsWith("leg_")) p.xRot = (idx(n) % 2 == 0 ? 0.5F : -0.5F) * k;
        else if (n.startsWith("head")) p.xRot = 0.6F * k;
        else if (n.startsWith("wing")) p.zRot = (n.endsWith("_l") ? 1 : -1) * 0.9F * k;
        else if (n.startsWith("orbit_") || n.equals("halo")) p.y += 8.0F * k;
    }

    private static int idx(String n) {
        int u = n.lastIndexOf('_');
        if (u < 0) return 0;
        try {
            return Integer.parseInt(n.substring(u + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
