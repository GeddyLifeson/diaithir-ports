package net.id.paradise_lost.client.rendering.entity.hostile;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.model.ParadiseLostModelLayers;
import net.id.paradise_lost.client.model.entity.SentinelEntityModel;
import net.id.paradise_lost.client.model.entity.SentinelEyesFeatureRenderer;
import net.id.paradise_lost.client.rendering.entity.state.SentinelRenderState;
import net.id.paradise_lost.entity.hostile.SentinelEntity;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;

public class SentinelEntityRenderer extends HumanoidMobRenderer<SentinelEntity, SentinelRenderState, SentinelEntityModel> {
    private static final Identifier TEXTURE = ModConstants.locate("textures/entity/sentinel/sentinel.png");

    public SentinelEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SentinelEntityModel(context.bakeLayer(ParadiseLostModelLayers.SENTINEL)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ParadiseLostModelLayers.SENTINEL_ARMOR, context.getModelSet(), SentinelEntityModel::new),
                context.getEquipmentRenderer()));
        this.addLayer(new SentinelEyesFeatureRenderer(this));
    }

    @Override
    public SentinelRenderState createRenderState() {
        return new SentinelRenderState();
    }

    @Override
    public void extractRenderState(SentinelEntity entity, SentinelRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.enlightened = entity.getEnlightened();
    }

    @Override
    public Identifier getTextureLocation(SentinelRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getShadowRadius(SentinelRenderState state) {
        return state.enlightened ? super.getShadowRadius(state) : 0F;
    }
}
