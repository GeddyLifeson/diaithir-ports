package net.id.paradise_lost.client.model.entity;

import net.id.paradise_lost.client.rendering.entity.state.SentinelRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Sentinels use the plain humanoid walk animation (arms swing, no raised arms), which is exactly what
 * the 26.1 {@link HumanoidModel#setupAnim} does for an empty arm pose.
 */
public class SentinelEntityModel extends HumanoidModel<SentinelRenderState> {
    public SentinelEntityModel(ModelPart root) {
        super(root);
    }
}
