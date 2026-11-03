package net.diaithir.bosses.client;

import net.diaithir.bosses.PlanetBoss;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;

/** Draws a planet boss with the model and texture of its own look (the entity's synced look id picks them). */
public final class PlanetBossRenderer extends MobRenderer<PlanetBoss, PlanetBossRenderState, LookModel> {
    public PlanetBossRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LookStore.model(""), 0.8F);
    }

    @Override
    public PlanetBossRenderState createRenderState() { return new PlanetBossRenderState(); }

    @Override
    public void extractRenderState(PlanetBoss e, PlanetBossRenderState s, float partialTicks) {
        super.extractRenderState(e, s, partialTicks);
        s.look = e.bossId();
    }

    @Override
    public Identifier getTextureLocation(PlanetBossRenderState s) { return LookStore.texture(s.look); }

    @Override
    public void submit(PlanetBossRenderState s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = LookStore.model(s.look);
        super.submit(s, poseStack, collector, camera);
    }
}
