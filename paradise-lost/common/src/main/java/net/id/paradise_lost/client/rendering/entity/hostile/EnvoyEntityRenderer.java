package net.id.paradise_lost.client.rendering.entity.hostile;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.id.paradise_lost.client.model.entity.EnvoyEyesFeatureRenderer;
import net.id.paradise_lost.client.rendering.entity.state.EnvoyRenderState;
import net.id.paradise_lost.entity.hostile.EnvoyEntity;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class EnvoyEntityRenderer extends AbstractSkeletonRenderer<EnvoyEntity, EnvoyRenderState> {
    private static final Identifier TEXTURE = ModConstants.locate("textures/entity/envoy/envoy.png");
    private static final Identifier TEXTURE_ENLIGHTENED = ModConstants.locate("textures/entity/envoy/envoy_enlightened.png");

    public EnvoyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, ParadiseLostModelLayers.ENVOY, ParadiseLostModelLayers.ENVOY_ARMOR);
        this.addLayer(new EnvoyEyesFeatureRenderer(this));
    }

    @Override
    public EnvoyRenderState createRenderState() {
        return new EnvoyRenderState();
    }

    @Override
    public void extractRenderState(EnvoyEntity entity, EnvoyRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.enlightened = entity.getEnlightened();
    }

    @Override
    public Identifier getTextureLocation(EnvoyRenderState state) {
        return state.enlightened ? TEXTURE_ENLIGHTENED : TEXTURE;
    }
}
