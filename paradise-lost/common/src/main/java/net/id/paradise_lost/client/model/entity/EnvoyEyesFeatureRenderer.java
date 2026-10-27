package net.id.paradise_lost.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.rendering.entity.state.EnvoyRenderState;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class EnvoyEyesFeatureRenderer extends EyesLayer<EnvoyRenderState, SkeletonModel<EnvoyRenderState>> {

    private static final RenderType TEXTURE = RenderTypes.eyes(ModConstants.locate("textures/entity/envoy/envoy_enlightened_eyes.png"));

    public EnvoyEyesFeatureRenderer(RenderLayerParent<EnvoyRenderState, SkeletonModel<EnvoyRenderState>> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, EnvoyRenderState state, float yRot, float xRot) {
        if (state.enlightened) {
            super.submit(poseStack, collector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return TEXTURE;
    }
}
