package net.diaithir.fauna.client;

import net.diaithir.fauna.FaunaMod;
import net.diaithir.fauna.Plan;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = FaunaMod.ID, value = Dist.CLIENT)
public final class FaunaClient {
    private FaunaClient() {}

    @SubscribeEvent
    static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        for (Plan p : Plan.ALL.values()) e.registerLayerDefinition(FaunaRenderer.layer(p), () -> PlanModel.layer(p));
    }

    @SubscribeEvent
    static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        FaunaMod.TYPES.forEach((n, t) -> e.registerEntityRenderer(t.get(), ctx -> new FaunaRenderer(ctx, Plan.ALL.get(n))));
    }
}
