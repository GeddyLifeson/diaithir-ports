package net.diaithir.worldsys.villageguards;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** The village guard is drawn as a player-model humanoid (it carries an iron sword; the model draws held items). */
final class GuardRenderer extends HumanoidMobRenderer<VillageGuard, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private static final Identifier SKIN = Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");

    GuardRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public HumanoidRenderState createRenderState() { return new HumanoidRenderState(); }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) { return SKIN; }
}
