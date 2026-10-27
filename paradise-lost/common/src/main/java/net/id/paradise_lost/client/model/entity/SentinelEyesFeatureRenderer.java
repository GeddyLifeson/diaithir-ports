package net.id.paradise_lost.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.rendering.entity.state.SentinelRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class SentinelEyesFeatureRenderer extends EyesLayer<SentinelRenderState, SentinelEntityModel> {

    private static final RenderType TEXTURE = RenderTypes.eyes(ModConstants.locate("textures/entity/sentinel/sentinel_eyes.png"));

    public SentinelEyesFeatureRenderer(RenderLayerParent<SentinelRenderState, SentinelEntityModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, SentinelRenderState state, float yRot, float xRot) {
        if (state.enlightened) {
            super.submit(poseStack, collector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return TEXTURE;
    }
}
