package rpggods;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import rpggods.network.CUpdateAltarPacket;
import rpggods.network.SAltarPacket;
import rpggods.network.SDeityPacket;
import rpggods.network.SOfferingPacket;
import rpggods.network.SPerkPacket;
import rpggods.network.SSacrificePacket;
import rpggods.network.SUpdateAltarPacket;
import rpggods.network.SUpdateSittingPacket;

public final class RGNetwork {
    private static final String PROTOCOL_VERSION = "4";

    private RGNetwork() {}

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar(RPGGods.MODID).versioned(PROTOCOL_VERSION);
        r.playToClient(SDeityPacket.TYPE, SDeityPacket.STREAM_CODEC, SDeityPacket::handlePacket);
        r.playToClient(SAltarPacket.TYPE, SAltarPacket.STREAM_CODEC, SAltarPacket::handlePacket);
        r.playToClient(SOfferingPacket.TYPE, SOfferingPacket.STREAM_CODEC, SOfferingPacket::handlePacket);
        r.playToClient(SSacrificePacket.TYPE, SSacrificePacket.STREAM_CODEC, SSacrificePacket::handlePacket);
        r.playToClient(SPerkPacket.TYPE, SPerkPacket.STREAM_CODEC, SPerkPacket::handlePacket);
        r.playToClient(SUpdateSittingPacket.TYPE, SUpdateSittingPacket.STREAM_CODEC, SUpdateSittingPacket::handlePacket);
        r.playToClient(SUpdateAltarPacket.TYPE, SUpdateAltarPacket.STREAM_CODEC, SUpdateAltarPacket::handlePacket);
        r.playToServer(CUpdateAltarPacket.TYPE, CUpdateAltarPacket.STREAM_CODEC, CUpdateAltarPacket::handlePacket);
    }
}
