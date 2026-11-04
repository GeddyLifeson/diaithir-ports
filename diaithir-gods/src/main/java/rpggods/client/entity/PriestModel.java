package rpggods.client.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.util.Mth;

/** Villager model plus a prayer idle: when the priest stands still the head bows slowly and the folded arms rise and settle. */
public class PriestModel extends VillagerModel {
    private final ModelPart head, arms;

    public PriestModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.arms = root.getChild("arms");
    }

    @Override
    public void setupAnim(VillagerRenderState s) {
        super.setupAnim(s);
        float still = 1.0F - Math.min(1.0F, s.walkAnimationSpeed * 4.0F);
        if (still <= 0.0F) return;
        float cycle = Mth.sin(s.ageInTicks * 0.04F);
        float bow = Math.max(0.0F, cycle);
        head.xRot += 0.45F * bow * still;
        arms.xRot -= 0.25F * bow * still;
    }
}
