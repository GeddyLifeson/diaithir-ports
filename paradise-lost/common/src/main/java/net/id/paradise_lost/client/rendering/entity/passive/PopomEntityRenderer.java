package net.id.paradise_lost.client.rendering.entity.passive;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.id.paradise_lost.client.model.entity.PopomEntityModel;
import net.id.paradise_lost.client.rendering.entity.state.PopomRenderState;
import net.id.paradise_lost.entity.passive.PopomEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class PopomEntityRenderer extends MobRenderer<PopomEntity, PopomRenderState, PopomEntityModel> {
    private static final Identifier TEXTURE = ModConstants.locate("textures/entity/popom/popom.png");

    public PopomEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PopomEntityModel(context.bakeLayer(ParadiseLostModelLayers.POPOM)), 0.7F);
    }

    @Override
    public PopomRenderState createRenderState() {
        return new PopomRenderState();
    }

    @Override
    public void extractRenderState(PopomEntity entity, PopomRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.furSize = entity.getFurSize();
        state.headAngle = entity.getHeadAngle(partialTicks);
    }

    @Override
    public Identifier getTextureLocation(PopomRenderState state) {
        return TEXTURE;
    }
}
