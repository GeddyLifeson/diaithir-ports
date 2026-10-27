package net.diaithir.furnishings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = DiaithirFurnishings.ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DiaithirFurnishings.SIGNPOST_ENTITY.get(), SignpostRenderer::new);
        event.registerBlockEntityRenderer(DiaithirFurnishings.JAR_ENTITY.get(), JarRenderer::new);
    }
}
