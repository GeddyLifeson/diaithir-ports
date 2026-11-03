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
        }
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
