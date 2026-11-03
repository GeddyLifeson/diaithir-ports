package net.diaithir.bosses.client;

import net.diaithir.bosses.Data;
import net.diaithir.bosses.PlanetBoss;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;

/** Draws a planet boss with the model and texture of its own look (the entity's synced look id picks them). */
public final class PlanetBossRenderer extends MobRenderer<Mob, PlanetBossRenderState, LookModel> {
    public PlanetBossRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, LookStore.model(""), 0.8F);
    }

    /** A planet boss carries its look id; another mod's creature that a profile gives our look is found by its entity type. */
    static String lookOf(Mob e) {
        if (e instanceof PlanetBoss pb) return pb.bossId();
        Data.Profile p = Data.byEntity(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());
        return p != null && p.look ? p.id : "";
    }

    @Override
    public PlanetBossRenderState createRenderState() { return new PlanetBossRenderState(); }

    @Override
    public void extractRenderState(Mob e, PlanetBossRenderState s, float partialTicks) {
        super.extractRenderState(e, s, partialTicks);
        s.look = lookOf(e);
        if (e instanceof PlanetBoss pb) {
            s.anim = pb.animType();
            s.animPhase = pb.animPhase();
            s.animT = pb.level().getGameTime() - pb.animStart() + partialTicks;
        } else {
            s.anim = "";
            s.animPhase = 0;
        }
    }

    @Override
    public Identifier getTextureLocation(PlanetBossRenderState s) { return LookStore.texture(s.look); }

    @Override
    public void submit(PlanetBossRenderState s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = LookStore.model(s.look);
        super.submit(s, poseStack, collector, camera);
    }
}
