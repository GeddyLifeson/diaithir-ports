package net.diaithir.fauna.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.diaithir.fauna.Plan;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Model built from a plan's JSON, with one generic animation: legs walk, wings flap, tails and fins sway, the head looks. */
public class PlanModel extends EntityModel<FaunaState> {
    private static final Pattern LIMB = Pattern.compile("(?:leg|arm)(?:_([fb]))?(?:_(\\d))?_([lr])");
    private final Plan plan;
    private final Map<String, ModelPart> parts = new HashMap<>();
    /** Limb segments hanging off another limb segment; they follow their parent instead of swinging. */
    private final java.util.Set<String> subLimb = new java.util.HashSet<>();

    public PlanModel(ModelPart root, Plan plan) {
        super(root);
        this.plan = plan;
        for (String r : plan.roots()) index(root, r);
    }

    private void index(ModelPart parent, String name) {
        ModelPart p = parent.getChild(name);
        parts.put(name, p);
        for (String c : plan.children(name)) {
            if (LIMB.matcher(name).matches() && LIMB.matcher(c).matches()) subLimb.add(c);
            index(p, c);
        }
    }

    public static LayerDefinition layer(Plan plan) {
        MeshDefinition mesh = new MeshDefinition();
        for (String r : plan.roots()) add(mesh.getRoot(), plan, r);
        return LayerDefinition.create(mesh, plan.json.get("tw").getAsInt(), plan.json.get("th").getAsInt());
    }

    private static void add(PartDefinition parent, Plan plan, String name) {
        JsonObject p = plan.part(name);
        JsonArray uv = p.getAsJsonArray("uv"), pv = p.getAsJsonArray("pivot"), rot = p.getAsJsonArray("rot");
        CubeListBuilder b = CubeListBuilder.create().texOffs(uv.get(0).getAsInt(), uv.get(1).getAsInt()).mirror(p.get("mirror").getAsBoolean());
        for (var e : p.getAsJsonArray("boxes")) {
            JsonArray x = e.getAsJsonArray();
            b.addBox(x.get(0).getAsFloat(), x.get(1).getAsFloat(), x.get(2).getAsFloat(), x.get(3).getAsFloat(), x.get(4).getAsFloat(), x.get(5).getAsFloat(),
                    new CubeDeformation(x.get(6).getAsFloat()));
        }
        PartDefinition d = parent.addOrReplaceChild(name, b, PartPose.offsetAndRotation(pv.get(0).getAsFloat(), pv.get(1).getAsFloat(), pv.get(2).getAsFloat(),
                rot.get(0).getAsFloat(), rot.get(1).getAsFloat(), rot.get(2).getAsFloat()));
        for (String c : plan.children(name)) add(d, plan, c);
    }

    @Override
    public void setupAnim(FaunaState s) {
        super.setupAnim(s);
        float pos = s.walkAnimationPos, speed = Math.min(1f, s.walkAnimationSpeed * 2f), t = s.ageInTicks;
        boolean swim = plan.water();
        for (var e : parts.entrySet()) {
            String n = e.getKey();
            ModelPart p = e.getValue();
            if (n.equals("head")) {
                p.yRot += s.yRot * Mth.DEG_TO_RAD * 0.7f;
                p.xRot += s.xRot * Mth.DEG_TO_RAD * 0.7f;
            } else if (LIMB.matcher(n).matches() && !subLimb.contains(n) && !swim && !s.flying) {
                // leg_l, leg_1_r, leg_f_2_l, arm_1_l ...: only the first segment of a limb swings, diagonal gait
                Matcher m = LIMB.matcher(n);
                m.matches();
                boolean right = m.group(3).equals("r");
                boolean arm = n.startsWith("arm");
                String fb = m.group(1);
                int idx = m.group(2) == null ? 1 : Integer.parseInt(m.group(2));
                boolean front = arm || (fb != null ? fb.equals("f") : idx % 2 == 0);
                float phase = (right == front) ? 0 : Mth.PI;
                p.xRot += Mth.cos(pos * 0.6662f + phase) * 1.1f * speed;
            } else if (n.startsWith("wing") && plan.fly()) {
                float w = s.flying ? Mth.sin(t * 1.1f) * 0.9f : 0.05f;
                p.zRot += n.endsWith("_r") ? -w : w;
            } else if (n.startsWith("tail_") || n.startsWith("fin_dorsal")) {
                int i = n.length() > 5 && Character.isDigit(n.charAt(5)) ? n.charAt(5) - '0' : 0;
                float amp = swim ? 0.35f * (0.3f + speed) : 0.06f + 0.15f * speed;
                p.yRot += Mth.sin(t * (swim ? 0.25f : 0.1f) - i * 0.7f) * amp;
            } else if (n.startsWith("fin_pec") || n.startsWith("fin_f")) {
                float w = Mth.sin(t * 0.2f) * 0.25f;
                p.zRot += n.endsWith("_r") ? -w : w;
            }
        }
    }
}
