package net.diaithir.fauna.client;

import net.diaithir.fauna.FaunaMob;
import net.diaithir.fauna.FaunaMod;
import net.diaithir.fauna.Plan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FaunaRenderer extends MobRenderer<FaunaMob, FaunaState, PlanModel> {
    private static final Map<Identifier, Boolean> EXISTS = new ConcurrentHashMap<>();
    private final Plan plan;

    public static ModelLayerLocation layer(Plan p) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(FaunaMod.ID, p.name), "main");
    }

    public FaunaRenderer(EntityRendererProvider.Context ctx, Plan plan) {
        super(ctx, new PlanModel(ctx.bakeLayer(layer(plan)), plan), Math.max(0.2f, plan.width * 0.4f));
        this.plan = plan;
    }

    @Override
    public FaunaState createRenderState() { return new FaunaState(); }

    @Override
    public void extractRenderState(FaunaMob mob, FaunaState state, float partial) {
        super.extractRenderState(mob, state, partial);
        state.species = mob.species();
        state.flying = plan.fly() && !mob.onGround();
    }

    @Override
    public Identifier getTextureLocation(FaunaState state) {
        if (state.species != null) {
            Identifier own = Identifier.fromNamespaceAndPath(FaunaMod.ID, "textures/entity/" + state.species.id + ".png");
            if (EXISTS.computeIfAbsent(own, id -> Minecraft.getInstance().getResourceManager().getResource(id).isPresent())) return own;
        }
        return Identifier.fromNamespaceAndPath(FaunaMod.ID, "textures/entity/plan/" + plan.name + ".png");
    }
}
