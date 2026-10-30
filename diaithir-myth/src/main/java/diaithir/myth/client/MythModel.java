package diaithir.myth.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * One model class for every creature. Parts are animated by the first letters of their names:
 * head*, legA and legB (two gait phases), armA and armB, wingP/wingN, tail*, segN (serpent chain).
 */
public class MythModel extends EntityModel<MythRenderState> {
    private record Named(String name, ModelPart part) {}

    private final List<Named> parts = new ArrayList<>();

    public MythModel(ModelPart root, ModelData data) {
        super(root);
        for (String n : data.names) {
            ModelPart p = root;
            for (String step : data.paths.get(n)) {
                p = p.getChild(step);
            }
            parts.add(new Named(n, p));
        }
    }

    @Override
    public void setupAnim(MythRenderState s) {
        super.setupAnim(s);
        float pos = s.walkAnimationPos, spd = Math.min(1.0F, s.walkAnimationSpeed);
        float a = Mth.cos(pos * 0.6662F) * 1.2F * spd;
        float t = s.ageInTicks;
        float swing = Mth.sin(s.swing * (float) Math.PI);
        int headIdx = 0;
        for (Named n : parts) {
            String nm = n.name();
            ModelPart p = n.part();
            if (nm.startsWith("head")) {
                p.yRot += s.yRot * ((float) Math.PI / 180F) * 0.9F + (headIdx > 0 ? Mth.sin(t * 0.05F + headIdx * 2.1F) * 0.25F : 0F);
                p.xRot += s.xRot * ((float) Math.PI / 180F) + swing * 0.5F;
                headIdx++;
            } else if (nm.startsWith("legA")) {
                p.xRot += s.flying ? 0.5F : a;
            } else if (nm.startsWith("legB")) {
                p.xRot += s.flying ? 0.5F : -a;
            } else if (nm.startsWith("armA")) {
                p.xRot += -a * 0.7F - swing * 1.6F;
            } else if (nm.startsWith("armB")) {
                p.xRot += a * 0.7F - swing * 1.6F;
            } else if (nm.equals("wingP") || nm.equals("wingN")) {
                float z = s.flying ? -Mth.sin(t * 1.1F) * 0.75F - 0.1F : 0.9F;
                p.zRot += nm.equals("wingP") ? z : -z;
            } else if (nm.startsWith("tail")) {
                p.yRot += Mth.sin(t * 0.12F) * 0.18F + a * 0.1F;
            } else if (nm.startsWith("seg")) {
                int i = Integer.parseInt(nm.substring(3));
                p.yRot += Mth.sin(t * 0.14F - i * 0.6F) * (0.12F + 0.25F * spd);
            }
        }
    }
}
