package net.diaithir.sharedspace.client;

import net.diaithir.sharedspace.SharedSpace;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = SharedSpace.ID, value = Dist.CLIENT)
public final class Client {
    private Client() {
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SharedSpace.CLUSTER_ENTITY.get(), ctx -> new ClusterRenderer());
    }
}
