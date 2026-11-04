package net.diaithir.worldsys.packversioncheck;

import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

/** Client side: tells the server this client's pack version as soon as it is in the world. */
final class PackVersionClient {
    static void init() {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class,
                e -> ClientPacketDistributor.sendToServer(new PackVersionPayload(PackVersionConstants.PACK_VERSION)));
    }

    private PackVersionClient() {}
}
