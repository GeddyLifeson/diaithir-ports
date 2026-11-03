// Ported from Talk Balloons (LGPL-3.0-only) https://github.com/CERBON-MODS/Talk-Balloons
// Modifications: RegisterPayloadHandlersEvent replaces Modern Networking registries
package net.diaithir.hudoverlays.talkballoons.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class TalkBalloonsNetwork {
    private TalkBalloonsNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            registrar.playToClient(BalloonPayload.TYPE, BalloonPayload.CODEC,
                    net.diaithir.hudoverlays.talkballoons.client.TalkBalloonsClient::handleBalloon);
        } else {
            registrar.playToClient(BalloonPayload.TYPE, BalloonPayload.CODEC, (payload, ctx) -> {});
        }
    }
}
