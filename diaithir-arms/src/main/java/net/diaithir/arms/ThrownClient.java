package net.diaithir.arms;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Draws thrown weapons as their item sprite. */
final class ThrownClient {
    static void init(IEventBus bus) {
        bus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> e.registerEntityRenderer(DiaithirArms.THROWN.get(), ThrownItemRenderer::new));
    }

    private ThrownClient() {}
}
