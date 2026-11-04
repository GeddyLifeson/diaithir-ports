package net.diaithir.creatureutilities.guards;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only renderer for the guard: a humanoid with our own redrawn skin. */
public final class GuardsClient {
    private static final Identifier SKIN = Identifier.fromNamespaceAndPath(net.diaithir.creatureutilities.CreatureUtilities.ID, "textures/entity/guard.png");

    private GuardsClient() {}

    public static void init(IEventBus bus) {
        bus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerEntityRenderer(Guards.GUARD.get(), ctx ->
            new HumanoidMobRenderer<GuardEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>>(ctx,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5F) {
                @Override public HumanoidRenderState createRenderState() { return new HumanoidRenderState(); }
                @Override public Identifier getTextureLocation(HumanoidRenderState s) { return SKIN; }
            }));
    }
}
