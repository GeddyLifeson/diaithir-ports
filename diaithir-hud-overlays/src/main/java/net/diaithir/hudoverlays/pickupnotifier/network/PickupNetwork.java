// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: payload registration through RegisterPayloadHandlersEvent (optional channel, server to client).
package net.diaithir.hudoverlays.pickupnotifier.network;

import net.diaithir.hudoverlays.pickupnotifier.client.ClientNet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PickupNetwork {
    private PickupNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        // the client class is only touched when the handler runs, which only happens on the client
        registrar.playToClient(TakeItemPayload.TYPE, TakeItemPayload.CODEC, (payload, context) -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                ClientNet.takeItem(payload.itemId(), payload.amount());
            }
        });
        registrar.playToClient(TakeItemStackPayload.TYPE, TakeItemStackPayload.CODEC, (payload, context) -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                ClientNet.takeItemStack(payload.itemStack());
            }
        });
    }
}
