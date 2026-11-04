package net.diaithir.vehicles.client;

import net.diaithir.vehicles.DiaithirVehicles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = DiaithirVehicles.ID, value = Dist.CLIENT)
public final class VehiclesClient {
    private VehiclesClient() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(DiaithirVehicles.TRACKED_CHASSIS.get(), ChassisRenderer::new);
    }
}
