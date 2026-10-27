package net.id.paradise_lost.client.rendering.entity.hostile;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.id.paradise_lost.client.model.entity.QuintEntityModel;
import net.id.paradise_lost.client.rendering.entity.state.QuintRenderState;
import net.id.paradise_lost.entity.passive.QuintEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class QuintEntityRenderer extends MobRenderer<QuintEntity, QuintRenderState, QuintEntityModel> {

    private static final Identifier TEXTURE = ModConstants.locate("textures/entity/quint.png");

    public QuintEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new QuintEntityModel(context.bakeLayer(ParadiseLostModelLayers.QUINT)), 0.0F);
    }

    @Override
    protected int getBlockLightLevel(QuintEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public QuintRenderState createRenderState() {
        return new QuintRenderState();
    }

    @Override
    public Identifier getTextureLocation(QuintRenderState state) {
        return TEXTURE;
    }
}
